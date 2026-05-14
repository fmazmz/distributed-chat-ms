package org.fmazmz.authmanager.api;

public record WebAuthnLoginStartResponse(String ceremonyId, String publicKeyCredentialRequestOptionsJson) {}
