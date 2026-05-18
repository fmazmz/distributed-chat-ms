package org.fmazmz.analyticssvc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter
@Setter
public class MessageSentEvent {
    @Id
    private UUID eventId;
    private UUID messageId;
    private UUID chatSessionId;
    private UUID senderId;
    private Instant createdAt;
    private int contentLengthChars;
}
