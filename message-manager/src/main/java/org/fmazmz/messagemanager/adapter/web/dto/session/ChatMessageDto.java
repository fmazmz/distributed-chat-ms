package org.fmazmz.messagemanager.adapter.web.dto.session;

import java.util.UUID;

public record ChatMessageDto(UUID sessionId, String content) {}