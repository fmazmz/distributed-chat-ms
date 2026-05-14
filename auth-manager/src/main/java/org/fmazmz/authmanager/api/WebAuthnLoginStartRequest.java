package org.fmazmz.authmanager.api;

import jakarta.validation.constraints.NotBlank;

public record WebAuthnLoginStartRequest(@NotBlank String userId) {}
