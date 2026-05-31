package org.fmazmz.messagemanager.service;

import java.util.UUID;

public interface UserProfilePort {

    void assertUserExists(UUID userId);

    String getUserName(UUID userId);
}
