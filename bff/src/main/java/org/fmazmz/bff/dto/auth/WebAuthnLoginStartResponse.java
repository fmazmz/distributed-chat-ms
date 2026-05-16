package org.fmazmz.bff.dto.auth;

public record WebAuthnLoginStartResponse(String ceremonyId, String publicKeyCredentialRequestOptionsJson) {}
