package org.fmazmz.messagemanager.service;

import org.fmazmz.messagemanager.exception.SenderNotFoundException;

import java.util.UUID;

/**
 * Outbound port: resolve user identity from User Service (gRPC).
 */
public interface UserProfilePort {

    /**
     * Ensures the user exists upstream. Implementations map gRPC NOT_FOUND to {@link
     * SenderNotFoundException}.
     */
    void assertUserExists(UUID userId);

    /**
     * Retrieves the username from the gRPC server call.
     */
    String getUserName(UUID userId);
}
