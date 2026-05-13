package org.fmazmz.userservice.user.dto;

import java.util.UUID;

public record NewUserResponse(
        UUID id,
        String userName,
        String email
) {
}
