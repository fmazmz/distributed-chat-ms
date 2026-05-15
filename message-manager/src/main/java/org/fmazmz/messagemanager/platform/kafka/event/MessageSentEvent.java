package org.fmazmz.messagemanager.platform.kafka.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka payload deliberately omits {@code content}: privacy-conscious consumers get audit metadata only.
 */
public record MessageSentEvent(
        UUID messageId,
        UUID chatSessionId,
        UUID senderId,
        Instant createdAt,
        int contentLengthChars
) {
}
