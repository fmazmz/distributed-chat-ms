package org.fmazmz.messagemanager.application.user;

import java.util.UUID;

/**
 * Outbound port: resolve user identity from User Service (gRPC).
 */
public interface UserProfilePort {

    /**
     * Ensures the user exists upstream. Implementations map gRPC NOT_FOUND to {@link
     * org.fmazmz.messagemanager.domain.exception.SenderNotFoundException}.
     */
    void assertUserExists(UUID userId);
}
