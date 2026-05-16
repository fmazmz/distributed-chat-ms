package org.fmazmz.messagemanager.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtUtils {

    private final JwtDecoder jwtDecoder;

    public JwtUtils(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public UUID subjectAsUuid(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public UUID validateTokenAndGetUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer "
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return UUID.fromString(jwt.getSubject());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}