package org.fmazmz.messagemanager.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class Session {
    private final UUID id;
    private final WebSocketSession webSocketSession;
    private final UUID firstUserId;
    private final UUID secondUserId;
    private final Instant createdAt;

    public Session(UUID id, WebSocketSession webSocketSession, UUID firstUserId, UUID secondUserId, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.webSocketSession = webSocketSession;
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
        this.createdAt = createdAt;
    }
}
