package org.fmazmz.messagemanager.exception;

import java.util.UUID;

public class SenderNotFoundException extends RuntimeException {

    public SenderNotFoundException(UUID senderId) {
        super("Sender user not found: " + senderId);
    }
}
