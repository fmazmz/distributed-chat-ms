package org.fmazmz.messagemanager.adapter.out.grpc;

import org.fmazmz.messagemanager.application.user.UserProfilePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Used when {@code messagemanager.user-grpc.validate-sender=false} (e.g. tests without User Service).
 */
@Component
@ConditionalOnProperty(name = "messagemanager.user-grpc.validate-sender", havingValue = "false")
public class NoOpUserProfileClient implements UserProfilePort {

    @Override
    public void assertUserExists(UUID userId) {
        // intentionally empty
    }
}
