package org.fmazmz.messagemanager.application.message.dto;

import org.fmazmz.messagemanager.domain.model.Message;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID roomId,
        UUID senderId,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRoom().getId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt());
    }
}
