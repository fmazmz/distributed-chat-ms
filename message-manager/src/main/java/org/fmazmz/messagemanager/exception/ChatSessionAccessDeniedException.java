package org.fmazmz.messagemanager.exception;

import java.util.UUID;

public final class ChatSessionAccessDeniedException extends RuntimeException {

    public ChatSessionAccessDeniedException(UUID sessionId, UUID userId) {
        super("User " + userId + " is not a participant in session " + sessionId);
    }
}
