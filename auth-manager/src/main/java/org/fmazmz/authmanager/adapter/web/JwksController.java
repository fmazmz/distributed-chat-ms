package org.fmazmz.authmanager.adapter.web;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.config.AuthProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final KeyPair jwtSigningKeyPair;
    private final AuthProperties authProperties;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        RSAKey rsaKey =
                new RSAKey.Builder((RSAPublicKey) jwtSigningKeyPair.getPublic())
                        .keyID(authProperties.getJwt().getKeyId())
                        .algorithm(JWSAlgorithm.RS256)
                        .keyUse(KeyUse.SIGNATURE)
                        .build();
        return new JWKSet(rsaKey).toPublicJWKSet().toJSONObject(false);
    }
}
