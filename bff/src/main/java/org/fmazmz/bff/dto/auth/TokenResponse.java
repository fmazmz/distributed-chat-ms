package org.fmazmz.bff.dto.auth;

public record TokenResponse(String userId, String accessToken, long expiresInSeconds, String tokenType) {}
