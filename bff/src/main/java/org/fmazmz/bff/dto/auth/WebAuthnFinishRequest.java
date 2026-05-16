package org.fmazmz.bff.dto.auth;

public record WebAuthnFinishRequest(String ceremonyId, String credentialJson) {}
