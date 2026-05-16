package org.fmazmz.bff.dto.user;

import java.util.UUID;

public record NewUserResponse(UUID id, String userName, String email) {}
