package org.fmazmz.userservice.user.api.dto;

import java.util.UUID;

public record NewUserResponse(
        UUID id,
        String userName,
        String email
) {
}
