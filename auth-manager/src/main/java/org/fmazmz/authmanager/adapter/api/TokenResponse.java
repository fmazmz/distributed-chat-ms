package org.fmazmz.authmanager.adapter.api;

public record TokenResponse(
        String userId, String accessToken, long expiresInSeconds, String tokenType) {}
