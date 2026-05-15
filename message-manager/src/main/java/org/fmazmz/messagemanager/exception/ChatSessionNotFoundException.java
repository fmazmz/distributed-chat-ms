package org.fmazmz.messagemanager.exception;

import java.util.UUID;

public final class ChatSessionNotFoundException extends RuntimeException {

    public ChatSessionNotFoundException(UUID sessionId) {
        super("Chat session not found: " + sessionId);
    }
}
