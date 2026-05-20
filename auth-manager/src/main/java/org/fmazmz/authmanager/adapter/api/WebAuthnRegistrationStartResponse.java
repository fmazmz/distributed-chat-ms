package org.fmazmz.authmanager.adapter.api;

public record WebAuthnRegistrationStartResponse(String ceremonyId, String publicKeyCredentialCreationOptionsJson) {}
