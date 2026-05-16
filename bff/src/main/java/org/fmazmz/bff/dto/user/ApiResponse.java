package org.fmazmz.bff.dto.user;

import java.time.LocalDateTime;

public record ApiResponse<T>(T data, String operationId, LocalDateTime timestamp) {}
