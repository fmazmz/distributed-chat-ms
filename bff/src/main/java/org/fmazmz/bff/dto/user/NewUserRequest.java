package org.fmazmz.bff.dto.user;

import java.util.UUID;

public record NewUserRequest(UUID id, String userName, String email) {}
