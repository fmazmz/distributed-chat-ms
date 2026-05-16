package org.fmazmz.bff.dto.auth;

public record WebAuthnRegistrationStartResponse(String ceremonyId, String publicKeyCredentialCreationOptionsJson) {}
