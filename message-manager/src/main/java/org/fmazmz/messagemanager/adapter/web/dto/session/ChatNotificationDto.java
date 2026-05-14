package org.fmazmz.messagemanager.adapter.web.dto.session;

import java.util.UUID;

public record ChatNotificationDto(UUID sessionId, UUID fromUserId) {}