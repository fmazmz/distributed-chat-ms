package org.fmazmz.messagemanager.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ChatSession {

    private final UUID sessionId;

    private final UUID requesterId;
    private WebSocketSession requesterWs;
    private final String requesterIp;

    private final UUID recipientId;
    private WebSocketSession recipientWs;
    private String recipientIp;

    private ChatStatus status = ChatStatus.PENDING;
    private final Instant createdAt = Instant.now();

    public ChatSession(UUID sessionId, UUID requesterId, String requesterIp, UUID recipientId) {
        this.sessionId = sessionId;
        this.requesterId = requesterId;
        this.requesterIp = requesterIp;
        this.recipientId = recipientId;
    }

    public boolean containsUser(UUID userId) {
        return requesterId.equals(userId) || recipientId.equals(userId);
    }
}