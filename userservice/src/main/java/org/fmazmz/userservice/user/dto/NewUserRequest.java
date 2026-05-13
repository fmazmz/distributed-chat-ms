package org.fmazmz.userservice.user.dto;

public record NewUserRequest(
        String userName,
        String email
) {
}
