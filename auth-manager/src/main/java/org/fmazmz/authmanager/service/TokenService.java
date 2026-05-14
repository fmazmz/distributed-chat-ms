package org.fmazmz.authmanager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.api.TokenResponse;
import org.fmazmz.authmanager.config.AuthProperties;
import org.fmazmz.authmanager.domain.RefreshToken;
import org.fmazmz.authmanager.repository.RefreshTokenRepository;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public TokenResponse issueForUser(String userId) {
        Instant now = Instant.now();
        var jwtProps = authProperties.getJwt();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProps.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProps.getAccessTokenTtl()))
                .subject(userId)
                .claim("token_use", "access")
                .build();

        var jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
        String refreshRaw = mintRefreshToken();
        persistRefreshToken(userId, refreshRaw, now);

        return new TokenResponse(
                jwt.getTokenValue(),
                jwtProps.getAccessTokenTtl().toSeconds(),
                refreshRaw,
                "Bearer");
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        String hash = sha256Hex(rawRefreshToken);

        RefreshToken existing = refreshTokenRepository
                .findByTokenHashAndRevokedIsFalse(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expired refresh token");
        }
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return issueForUser(existing.getUserId());
    }

    private void persistRefreshToken(String userId, String raw, Instant now) {
        var entity = new RefreshToken();
        entity.setId(UUID.randomUUID());
        entity.setTokenHash(sha256Hex(raw));
        entity.setUserId(userId);
        entity.setExpiresAt(now.plus(authProperties.getJwt().getRefreshTokenTtl()));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);
    }

    private String mintRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
