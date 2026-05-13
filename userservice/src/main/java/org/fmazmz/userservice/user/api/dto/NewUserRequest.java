package org.fmazmz.userservice.user.api.dto;

public record NewUserRequest(
        String userName,
        String email
) {
}
