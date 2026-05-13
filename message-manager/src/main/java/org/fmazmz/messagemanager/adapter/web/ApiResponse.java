package org.fmazmz.messagemanager.adapter.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Standard API response wrapper.")
public record ApiResponse<T>(
        @Schema(description = "Response payload data")
        T data,

        @Schema(description = "Unique operation identifier for tracking")
        String operationId,

        @Schema(description = "Timestamp of the response")
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, UUID.randomUUID().toString(), LocalDateTime.now());
    }
}
