package org.fmazmz.bff.websocket;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.bff.config.BffProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class MessageManagerWebSocketProxy extends TextWebSocketHandler {

    private static final String PEER_KEY = "ws-peer";
    private static final String PENDING_KEY = "ws-pending";
    private static final String USER_ID_ATTR = "wsUserId";
    private static final String BROWSER_REG_ATTR = "wsBrowserReg";
    private static final Set<String> CLIENT_MESSAGE_TYPES = Set.of("REQUEST_CHAT", "ACCEPT_CHAT", "MESSAGE");
    private static final Pattern SUB_CLAIM = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");

    private final String messageManagerWsBase;
    private final StandardWebSocketClient client = new StandardWebSocketClient();
    private final ConcurrentHashMap<String, Object> sendLocks = new ConcurrentHashMap<>();
    /** Latest open browser tab per JWT sub — avoids forwarding to stale peer links. */
    private final ConcurrentHashMap<String, WebSocketSession> activeBrowserByUser = new ConcurrentHashMap<>();

    public MessageManagerWebSocketProxy(BffProperties properties) {
        this.messageManagerWsBase = properties.getMessageManagerWebSocketUrl().replaceAll("/$", "");
        log.info("WebSocket proxy /ws/chat -> {}", messageManagerWsBase);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession browserSession) {
        String userId = subFromQuery(browserSession.getUri() != null ? browserSession.getUri().getQuery() : null);
        String browserReg = UUID.randomUUID().toString();
        browserSession.getAttributes().put(USER_ID_ATTR, userId);
        browserSession.getAttributes().put(BROWSER_REG_ATTR, browserReg);

        if (userId != null) {
            WebSocketSession previous = activeBrowserByUser.get(userId);
            if (previous != null && previous.isOpen() && !previous.getId().equals(browserSession.getId())) {
                log.info(
                        "Closing stale browser WS for userId={} oldSession={} newSession={}",
                        userId,
                        previous.getId(),
                        browserSession.getId());
                closeQuietly(previous, CloseStatus.GOING_AWAY);
            }
            activeBrowserByUser.put(userId, browserSession);
        }

        String query = browserSession.getUri().getQuery();
        String target = messageManagerWsBase + (query != null && !query.isBlank() ? "?" + query : "");
        log.info(
                "Browser WS connected userId={} sessionId={}, opening backend {}",
                userId,
                browserSession.getId(),
                target);

        TextWebSocketHandler backendHandler = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession backendSession) throws IOException {
                link(browserSession, backendSession, userId);
                flushPending(browserSession, backendSession);
                log.info(
                        "Backend WS connected for userId={} browserSession={} backendSession={}",
                        userId,
                        browserSession.getId(),
                        backendSession.getId());
            }

            @Override
            protected void handleTextMessage(WebSocketSession backendSession, TextMessage message) throws IOException {
                WebSocketSession browser = resolveActiveBrowser(backendSession);
                if (browser == null || !browser.isOpen()) {
                    log.warn(
                            "No active browser for userId={} backendSession={} frame={}",
                            backendSession.getAttributes().get(USER_ID_ATTR),
                            backendSession.getId(),
                            message.getPayload());
                    return;
                }
                log.info(
                        "WS proxy MM -> browser userId={} browserSession={}: {}",
                        backendSession.getAttributes().get(USER_ID_ATTR),
                        browser.getId(),
                        message.getPayload());
                sendTo(browser, message);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession backendSession, CloseStatus status) {
                sendLocks.remove(backendSession.getId());
                closeBackendPeer(backendSession, status);
            }

            @Override
            public void handleTransportError(WebSocketSession backendSession, Throwable exception) {
                log.warn("Backend WS error: {}", exception.getMessage());
                closeBackendPeer(backendSession, CloseStatus.SERVER_ERROR);
            }
        };

        client.execute(backendHandler, new WebSocketHttpHeaders(), URI.create(target))
                .whenComplete((ignored, error) -> {
                    if (error != null) {
                        log.error("Failed to connect backend WebSocket at {}", target, error);
                        closeQuietly(browserSession, CloseStatus.SERVER_ERROR);
                    }
                });
    }

    @Override
    protected void handleTextMessage(WebSocketSession browserSession, TextMessage message) throws IOException {
        String payload = message.getPayload();
        if (!isClientToServerMessage(payload)) {
            log.warn("Dropped non-client WS frame on browser leg (echo/loopback): {}", payload);
            return;
        }
        WebSocketSession backend = peer(browserSession);
        log.info(
                "WS proxy browser -> message-manager userId={} sessionId={}: {}",
                browserSession.getAttributes().get(USER_ID_ATTR),
                browserSession.getId(),
                payload);
        if (backend != null && backend.isOpen()) {
            sendTo(backend, message);
            return;
        }
        pending(browserSession).add(message);
        log.debug("Queued WS message until backend is ready ({} pending)", pending(browserSession).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession browserSession, CloseStatus status) {
        String userId = (String) browserSession.getAttributes().get(USER_ID_ATTR);
        String browserReg = (String) browserSession.getAttributes().get(BROWSER_REG_ATTR);
        log.info("Browser WS closed userId={} sessionId={}: {}", userId, browserSession.getId(), status);
        if (userId != null && browserReg != null) {
            activeBrowserByUser.computeIfPresent(userId, (id, open) ->
                    browserReg.equals(open.getAttributes().get(BROWSER_REG_ATTR)) ? null : open);
        }
        sendLocks.remove(browserSession.getId());
        closeBackendPeer(browserSession, status);
    }

    @Override
    public void handleTransportError(WebSocketSession browserSession, Throwable exception) {
        log.warn("Browser WS error: {}", exception.getMessage());
        closeBackendPeer(browserSession, CloseStatus.SERVER_ERROR);
    }

    private WebSocketSession resolveActiveBrowser(WebSocketSession backendSession) {
        Object userId = backendSession.getAttributes().get(USER_ID_ATTR);
        if (userId instanceof String uid) {
            WebSocketSession latest = activeBrowserByUser.get(uid);
            if (latest != null && latest.isOpen()) {
                return latest;
            }
        }
        return peer(backendSession);
    }

    static String subFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String token = null;
        for (String part : query.split("&")) {
            if (part.startsWith("token=")) {
                token = part.substring("token=".length());
                break;
            }
        }
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] segments = token.split("\\.");
        if (segments.length < 2) {
            return null;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(segments[1]), StandardCharsets.UTF_8);
            Matcher matcher = SUB_CLAIM.matcher(payload);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return null;
    }

    private void sendTo(WebSocketSession session, TextMessage message) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        Object lock = sendLocks.computeIfAbsent(session.getId(), ignored -> new Object());
        synchronized (lock) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }

    private boolean isClientToServerMessage(String payload) {
        if (payload == null || payload.isBlank() || !payload.contains("\"data\"")) {
            return false;
        }
        for (String type : CLIENT_MESSAGE_TYPES) {
            if (payload.contains("\"type\":\"" + type + "\"")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static List<TextMessage> pending(WebSocketSession browserSession) {
        Object list = browserSession.getAttributes().computeIfAbsent(PENDING_KEY, k -> new ArrayList<TextMessage>());
        return (List<TextMessage>) list;
    }

    private void flushPending(WebSocketSession browserSession, WebSocketSession backendSession) throws IOException {
        List<TextMessage> pending = pending(browserSession);
        for (TextMessage message : pending) {
            sendTo(backendSession, message);
        }
        pending.clear();
    }

    private static void link(WebSocketSession browser, WebSocketSession backend, String userId) {
        browser.getAttributes().put(PEER_KEY, backend);
        backend.getAttributes().put(PEER_KEY, browser);
        if (userId != null) {
            backend.getAttributes().put(USER_ID_ATTR, userId);
        }
    }

    private static WebSocketSession peer(WebSocketSession session) {
        Object peer = session.getAttributes().get(PEER_KEY);
        return peer instanceof WebSocketSession webSocketSession ? webSocketSession : null;
    }

    /** Close only the backend peer; do not close the user's current browser tab in {@link #activeBrowserByUser}. */
    private void closeBackendPeer(WebSocketSession session, CloseStatus status) {
        WebSocketSession backendOrBrowser = peer(session);
        session.getAttributes().remove(PEER_KEY);
        session.getAttributes().remove(PENDING_KEY);
        if (backendOrBrowser != null) {
            backendOrBrowser.getAttributes().remove(PEER_KEY);
            sendLocks.remove(backendOrBrowser.getId());
            closeQuietly(backendOrBrowser, status);
        }
    }

    private static void closeQuietly(WebSocketSession session, CloseStatus status) {
        if (session != null && session.isOpen()) {
            try {
                session.close(status);
            } catch (IOException ignored) {
                // best effort
            }
        }
    }
}
