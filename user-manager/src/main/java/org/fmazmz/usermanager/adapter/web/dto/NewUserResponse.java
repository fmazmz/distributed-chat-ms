package org.fmazmz.usermanager.adapter.web.dto;

import java.util.UUID;

public record NewUserResponse(
        UUID id,
        String userName,
        String email
) {
}
