package org.fmazmz.authmanager.adapter.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebAuthnRegistrationStartRequest(
        @NotBlank @Size(min = 3, max = 55) String userName, @NotBlank @Email String email) {}
