package org.fmazmz.authmanager.adapter.api;

import jakarta.validation.constraints.NotBlank;

public record WebAuthnFinishRequest(@NotBlank String ceremonyId, @NotBlank String credentialJson) {}
