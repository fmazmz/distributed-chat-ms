package org.fmazmz.messagemanager.adapter.web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fmazmz.messagemanager.security.JwtUtils;
import org.fmazmz.messagemanager.service.MessageApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final MessageApplicationService messageApplicationService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    public ChatHandler(MessageApplicationService messageApplicationService, JwtUtils jwtUtils) {
        this.messageApplicationService = messageApplicationService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        UUID userId = jwtUtils.validateTokenAndGetUserId(authHeader);
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        synchronized (sessions) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(message);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}