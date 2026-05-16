package org.fmazmz.authmanager.api;

/** JWT issued after successful WebAuthn; {@code userId} is the canonical account UUID for User Service profile creation. */
public record TokenResponse(
        String userId, String accessToken, long expiresInSeconds, String tokenType) {}
