package org.fmazmz.messagemanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
public class ChatSessionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID requesterId;

    @Column(nullable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status;

    @CreationTimestamp
    private Instant createdAt;

    public ChatSessionEntity(UUID id, UUID requesterId, UUID recipientId, ChatStatus status) {
        this.id = id;
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = status;
    }

    public boolean hasParticipant(UUID userId) {
        return requesterId.equals(userId) || recipientId.equals(userId);
    }
}
