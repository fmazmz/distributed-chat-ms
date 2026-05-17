package org.fmazmz.messagemanager.adapter.web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.messagemanager.adapter.web.dto.session.AcceptChatDto;
import org.fmazmz.messagemanager.adapter.web.dto.session.ChatMessageDto;
import org.fmazmz.messagemanager.adapter.web.dto.session.ChatRequestDto;
import org.fmazmz.messagemanager.exception.ChatSessionAccessDeniedException;
import org.fmazmz.messagemanager.exception.ChatSessionNotFoundException;
import org.fmazmz.messagemanager.exception.MessageOperationException;
import org.fmazmz.messagemanager.exception.SenderNotFoundException;
import org.fmazmz.messagemanager.model.ChatSession;
import org.fmazmz.messagemanager.model.ChatStatus;
import org.fmazmz.messagemanager.security.JwtUtils;
import org.fmazmz.messagemanager.service.ChatSessionLifecycleService;
import org.fmazmz.messagemanager.service.MessageApplicationService;
import org.fmazmz.messagemanager.service.UserProfilePort;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final JwtUtils jwtUtils;
    private final UserProfilePort userProfileClient;
    private final ChatSessionLifecycleService chatSessionLifecycleService;
    private final MessageApplicationService messageApplicationService;

    static final String USER_ID_ATTR = "userId";
    static final String REGISTRATION_ID_ATTR = "wsRegistrationId";

    private static final int SEND_TIME_LIMIT_MS = 10_000;
    private static final int SEND_BUFFER_SIZE_LIMIT = 512 * 1024;

    private final Map<UUID, WebSocketSession> activeConnections = new ConcurrentHashMap<>();
    private final Map<UUID, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final Map<String, Object> sendLocks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatHandler(
            JwtUtils jwtUtils,
            UserProfilePort userProfileClient,
            ChatSessionLifecycleService chatSessionLifecycleService,
            MessageApplicationService messageApplicationService
    ) {
        this.jwtUtils = jwtUtils;
        this.userProfileClient = userProfileClient;
        this.chatSessionLifecycleService = chatSessionLifecycleService;
        this.messageApplicationService = messageApplicationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            Object attr = session.getAttributes().get(ChatJwtHandshakeInterceptor.BEARER_ATTRIBUTE);
            if (attr instanceof String bearer) {
                authHeader = bearer;
            }
        }
        final UUID userId;
        try {
            userId = jwtUtils.validateTokenAndGetUserId(authHeader);
        } catch (RuntimeException ex) {
            log.warn("WebSocket auth failed: {}", ex.getMessage());
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }
        String registrationId = UUID.randomUUID().toString();
        session.getAttributes().put(USER_ID_ATTR, userId);
        session.getAttributes().put(REGISTRATION_ID_ATTR, registrationId);

        WebSocketSession previous = activeConnections.get(userId);
        if (previous != null && previous.isOpen()) {
            log.info("Closing previous WebSocket for userId={} (new tab/login)", userId);
            previous.close(CloseStatus.GOING_AWAY);
        }

        WebSocketSession outbound =
                new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, SEND_BUFFER_SIZE_LIMIT);
        outbound.getAttributes().put(USER_ID_ATTR, userId);
        outbound.getAttributes().put(REGISTRATION_ID_ATTR, registrationId);
        activeConnections.put(userId, outbound);

        String userName = resolveUserName(userId);
        log.info(
                "WebSocket connected: userId={} userName={} onlineUsers={}",
                userId,
                userName,
                activeConnections.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession sender, TextMessage message) throws Exception {
        var json = objectMapper.readTree(message.getPayload());
        if (!json.has("data") || !json.get("data").isObject()) {
            log.debug("Ignoring non-client WS frame (server push/echo): {}", message.getPayload());
            return;
        }
        String type = json.get("type").asText();

        switch (type) {
            case "REQUEST_CHAT" -> {
                ChatRequestDto dto = objectMapper.treeToValue(json.get("data"), ChatRequestDto.class);
                handleRequestChat(sender, dto);
            }
            case "ACCEPT_CHAT" -> {
                AcceptChatDto dto = objectMapper.treeToValue(json.get("data"), AcceptChatDto.class);
                handleAcceptChat(sender, dto);
            }
            case "MESSAGE" -> {
                ChatMessageDto dto = objectMapper.treeToValue(json.get("data"), ChatMessageDto.class);
                handleChatMessage(sender, dto);
            }
            default -> sendJson(sender, Map.of("type", "ERROR", "message", "Unsupported message type: " + type));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession ws, CloseStatus status) {
        Object userIdAttr = ws.getAttributes().get(USER_ID_ATTR);
        if (userIdAttr instanceof UUID userId) {
            Object closedRegistration = ws.getAttributes().get(REGISTRATION_ID_ATTR);
            activeConnections.computeIfPresent(userId, (id, open) -> {
                Object openRegistration = open.getAttributes().get(REGISTRATION_ID_ATTR);
                if (closedRegistration != null && closedRegistration.equals(openRegistration)) {
                    sendLocks.remove(open.getId());
                    return null;
                }
                return open;
            });
            log.info("WebSocket closed: userId={} onlineUsers={}", userId, activeConnections.size());
        } else {
            sendLocks.remove(ws.getId());
        }

        chatSessions.values().removeIf(session ->
                session.getRequesterWs() == ws || session.getRecipientWs() == ws
        );
    }

    private void handleRequestChat(WebSocketSession sender, ChatRequestDto dto) throws IOException {
        UUID requesterId = getUserId(sender);
        UUID recipientId = dto.toUserId();
        String requesterIp = getClientIp(sender);

        UUID sessionId = UUID.randomUUID();
        ChatSession session = new ChatSession(sessionId, requesterId, requesterIp, recipientId);
        session.setRequesterWs(sender);
        chatSessions.put(sessionId, session);
        chatSessionLifecycleService.registerPending(sessionId, requesterId, recipientId);

        sendJson(sender, Map.of(
                "type", "CHAT_INVITE_SENT",
                "sessionId", sessionId.toString(),
                "toUserId", recipientId.toString()));

        WebSocketSession recipientWs = activeConnections.get(recipientId);
        if (recipientWs != null && recipientWs.isOpen()) {
            sendJson(recipientWs, Map.of(
                    "type", "CHAT_REQUEST",
                    "sessionId", sessionId.toString(),
                    "fromUserId", requesterId.toString()));
            log.info(
                    "Chat invite delivered: sessionId={} from={} to={} recipientWsId={}",
                    sessionId,
                    requesterId,
                    recipientId,
                    recipientWs.getId());
        } else {
            log.warn("Chat invite not delivered — recipient {} is not online (online: {})", recipientId, activeConnections.keySet());
            sendJson(sender, Map.of(
                    "type", "CHAT_PEER_OFFLINE",
                    "toUserId", recipientId.toString(),
                    "message", "User is not connected to chat. They must open the app and stay on the chat screen."));
        }
    }

    private void handleAcceptChat(WebSocketSession sender, AcceptChatDto dto) throws IOException {
        UUID sessionId = dto.sessionId();
        ChatSession session = chatSessions.get(sessionId);
        if (session == null || !session.getRecipientId().equals(getUserId(sender))) {
            return;
        }
        try {
            chatSessionLifecycleService.markActive(sessionId, getUserId(sender));
        } catch (ChatSessionNotFoundException | ChatSessionAccessDeniedException ex) {
            log.warn("Accept chat failed: {}", ex.getMessage());
            sendJson(sender, Map.of("type", "ERROR", "message", ex.getMessage()));
            return;
        }
        session.setRecipientWs(sender);
        session.setRecipientIp(getClientIp(sender));
        session.setStatus(ChatStatus.ACTIVE);

        WebSocketSession requesterWs = session.getRequesterWs();
        if (requesterWs != null && requesterWs.isOpen()) {
            sendJson(requesterWs, Map.of("type", "CHAT_ACTIVE", "sessionId", session.getSessionId().toString()));
        }
    }

    private void handleChatMessage(WebSocketSession sender, ChatMessageDto dto) throws IOException {
        UUID sessionId = dto.sessionId();
        ChatSession session = chatSessions.get(sessionId);
        if (session == null || session.getStatus() != ChatStatus.ACTIVE) {
            sendText(sender, "Chat not active yet");
            return;
        }

        try {
            messageApplicationService.persistAndPublishChatMessage(sessionId, getUserId(sender), dto.content());
        } catch (MessageOperationException | ChatSessionNotFoundException | ChatSessionAccessDeniedException ex) {
            sendJson(sender, Map.of("type", "ERROR", "message", ex.getMessage()));
            return;
        } catch (SenderNotFoundException ex) {
            sendJson(sender, Map.of("type", "ERROR", "message", "Sender not verified"));
            return;
        }

        WebSocketSession recipient = sender == session.getRequesterWs() ? session.getRecipientWs() : session.getRequesterWs();
        if (recipient != null && recipient.isOpen()) {
            sendText(recipient, dto.content());
        }
    }

    private UUID getUserId(WebSocketSession ws) {
        Object attr = ws.getAttributes().get(USER_ID_ATTR);
        if (attr instanceof UUID userId) {
            return userId;
        }
        throw new IllegalStateException("WebSocket session has no user id");
    }

    private String getClientIp(WebSocketSession ws) {
        return ws.getRemoteAddress() != null ? ws.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private String resolveUserName(UUID userId) {
        try {
            return userProfileClient.getUserName(userId);
        } catch (RuntimeException ex) {
            log.warn("Could not resolve userName for userId={}: {}", userId, ex.getMessage());
            return userId.toString();
        }
    }

    private WebSocketSession sessionForSend(WebSocketSession session) {
        Object attr = session.getAttributes().get(USER_ID_ATTR);
        if (attr instanceof UUID userId) {
            WebSocketSession registered = activeConnections.get(userId);
            if (registered != null) {
                return registered;
            }
        }
        return session;
    }

    private void sendJson(WebSocketSession session, Map<String, ?> payload) throws IOException {
        sendText(session, objectMapper.writeValueAsString(payload));
    }

    private void sendText(WebSocketSession session, String text) throws IOException {
        WebSocketSession target = sessionForSend(session);
        if (!target.isOpen()) {
            return;
        }
        Object lock = sendLocks.computeIfAbsent(target.getId(), ignored -> new Object());
        synchronized (lock) {
            if (target.isOpen()) {
                target.sendMessage(new TextMessage(text));
            }
        }
    }
}