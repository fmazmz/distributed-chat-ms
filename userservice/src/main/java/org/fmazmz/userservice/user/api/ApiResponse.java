package org.fmazmz.userservice.user.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Standard API response wrapper. HTTP status is carried only on the response line, not duplicated here.")
public record ApiResponse<T>(
        @Schema(description = "Response payload data")
        T data,

        @Schema(description = "Unique operation identifier for tracking", example = "123e4567-e89b-12d3-a456-426614174000")
        String operationId,

        @Schema(description = "Timestamp of the response", example = "2024-12-06T10:30:00")
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, UUID.randomUUID().toString(), LocalDateTime.now());
    }
}