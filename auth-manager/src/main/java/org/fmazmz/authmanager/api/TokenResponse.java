package org.fmazmz.authmanager.api;

public record TokenResponse(
        String accessToken, long expiresInSeconds, String refreshToken, String tokenType) {}
