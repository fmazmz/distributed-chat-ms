package org.fmazmz.messagemanager.adapter.web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.messagemanager.adapter.web.dto.session.AcceptChatDto;
import org.fmazmz.messagemanager.adapter.web.dto.session.ChatMessageDto;
import org.fmazmz.messagemanager.adapter.web.dto.session.ChatRequestDto;
import org.fmazmz.messagemanager.model.ChatSession;
import org.fmazmz.messagemanager.model.ChatStatus;
import org.fmazmz.messagemanager.security.JwtUtils;
import org.fmazmz.messagemanager.service.UserProfilePort;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
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

    private final Map<UUID, WebSocketSession> activeConnections = new ConcurrentHashMap<>();
    private final Map<UUID, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatHandler(JwtUtils jwtUtils, UserProfilePort userProfileClient) {
        this.jwtUtils = jwtUtils;
        this.userProfileClient = userProfileClient;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Authenticate user
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        UUID userId = jwtUtils.validateTokenAndGetUserId(authHeader);
        String userName = userProfileClient.getUserName(userId);
        activeConnections.put(userId, session);

        log.info("Websocket connection established with clientIp: {}, userName: {}", getClientIp(session), userName);
    }

    @Override
    protected void handleTextMessage(WebSocketSession sender, TextMessage message) throws Exception {
        var json = objectMapper.readTree(message.getPayload());
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
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession ws, CloseStatus status) {
        // Remove from active connections
        activeConnections.values().removeIf(session -> session == ws);

        // Remove any chat sessions involving this user
        chatSessions.values().removeIf(session ->
                session.getRequesterWs() == ws || session.getRecipientWs() == ws
        );
    }

    private void handleRequestChat(WebSocketSession sender, ChatRequestDto dto) throws IOException {
        UUID requesterId = getUserId(sender);
        UUID recipientId = dto.toUserId();
        String requesterIp = getClientIp(sender);

        ChatSession session = new ChatSession(requesterId, requesterIp, recipientId);
        session.setRequesterWs(sender);
        chatSessions.put(session.getSessionId(), session);

        // Optionally notify recipient
        WebSocketSession recipientWs = activeConnections.get(recipientId);
        if (recipientWs != null && recipientWs.isOpen()) {
            recipientWs.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    Map.of("type", "CHAT_REQUEST", "sessionId", session.getSessionId(), "fromUserId", requesterId)
            )));
        }
    }

    private void handleAcceptChat(WebSocketSession sender, AcceptChatDto dto) {
        UUID sessionId = dto.sessionId();
        ChatSession session = chatSessions.get(sessionId);
        if (session != null && session.getRecipientId().equals(getUserId(sender))) {
            session.setRecipientWs(sender);
            session.setRecipientIp(getClientIp(sender));
            session.setStatus(ChatStatus.ACTIVE);
        }
    }

    private void handleChatMessage(WebSocketSession sender, ChatMessageDto dto) throws IOException {
        UUID sessionId = dto.sessionId();
        ChatSession session = chatSessions.get(sessionId);
        if (session == null || session.getStatus() != ChatStatus.ACTIVE) {
            sender.sendMessage(new TextMessage("Chat not active yet"));
            return;
        }

        WebSocketSession recipient = sender == session.getRequesterWs() ? session.getRecipientWs() : session.getRequesterWs();
        if (recipient != null && recipient.isOpen()) {
            recipient.sendMessage(new TextMessage(dto.content()));
        }
    }

    private UUID getUserId(WebSocketSession ws) {
        return activeConnections.entrySet().stream()
                .filter(e -> e.getValue() == ws)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    private String getClientIp(WebSocketSession ws) {
        return ws.getRemoteAddress() != null ? ws.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}