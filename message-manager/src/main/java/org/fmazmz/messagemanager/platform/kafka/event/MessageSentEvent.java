package org.fmazmz.messagemanager.platform.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record MessageSentEvent(
        UUID eventId,
        UUID messageId,
        UUID chatSessionId,
        UUID senderId,
        Instant createdAt,
        int contentLengthChars
) {
}
