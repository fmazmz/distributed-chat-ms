package org.fmazmz.authmanager.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.adapter.api.TokenResponse;
import org.fmazmz.authmanager.config.AuthProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;

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

        return new TokenResponse(
                userId, jwt.getTokenValue(), jwtProps.getAccessTokenTtl().toSeconds(), "Bearer");
    }
}
