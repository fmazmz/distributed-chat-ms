package org.fmazmz.usermanager.user.web.dto;

public record NewUserRequest(
        String userName,
        String email
) {
}
