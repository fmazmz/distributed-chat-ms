package org.fmazmz.authmanager.adapter.api;

public record WebAuthnLoginStartResponse(String ceremonyId, String publicKeyCredentialRequestOptionsJson) {}
