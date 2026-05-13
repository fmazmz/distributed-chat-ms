package org.fmazmz.usermanager.user.web.dto;

import java.util.UUID;

public record NewUserResponse(
        UUID id,
        String userName,
        String email
) {
}
