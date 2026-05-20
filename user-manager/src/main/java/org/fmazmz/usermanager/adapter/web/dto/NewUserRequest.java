package org.fmazmz.usermanager.adapter.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record NewUserRequest(
        @NotNull UUID id,
        @NotBlank @Size(min = 3, max = 55) String userName,
        @NotBlank @Email String email) {}
