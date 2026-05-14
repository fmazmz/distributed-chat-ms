package org.fmazmz.authmanager.api;

public record WebAuthnRegistrationStartResponse(String ceremonyId, String publicKeyCredentialCreationOptionsJson) {}
