package org.fmazmz.bff.dto.message;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID id, UUID chatSessionId, UUID senderId, String content, Instant createdAt) {}
