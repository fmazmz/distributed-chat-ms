package org.fmazmz.messagemanager.application.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMessageRequest(
        @NotNull UUID senderId,
        @NotBlank @Size(max = 5000) String content
) {
}
