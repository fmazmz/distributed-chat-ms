package org.fmazmz.authmanager.api;

import jakarta.validation.constraints.NotBlank;

public record WebAuthnRegistrationStartRequest(@NotBlank String userId, @NotBlank String displayName) {}
