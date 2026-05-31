package org.fmazmz.authmanager.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final AuthProperties authProperties;

    @Bean
    public KeyPair jwtSigningKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = resolvePemFromEnvOrProperties();
        if (pem == null || pem.isBlank()) {
            if (isProdProfile()) {
                throw new IllegalStateException(
                        "Set JWT_RSA_PRIVATE_KEY or JWT_RSA_PRIVATE_KEY_B64 when spring.profiles.active includes 'prod'");
            }
            log.warn(
                    "No JWT RSA private key in env: generating ephemeral RSA key. "
                            + "Tokens become invalid after restart; set JWT_RSA_PRIVATE_KEY(_B64) for production.");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }
        return loadPkcs8RsaFromPem(normalizePem(pem));
    }

    private String resolvePemFromEnvOrProperties() {
        var rsa = authProperties.getJwt().getRsa();
        String b64 = firstNonBlank(rsa.getPrivateKeyPemBase64(), System.getenv("JWT_RSA_PRIVATE_KEY_B64"));
        if (b64 != null && !b64.isBlank()) {
            return new String(Base64.getDecoder().decode(b64.trim()), StandardCharsets.UTF_8);
        }
        return firstNonBlank(rsa.getPrivateKeyPem(), System.getenv("JWT_RSA_PRIVATE_KEY"));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return "";
    }

    static String normalizePem(String pem) {
        return pem.trim().replace("\\n", "\n");
    }

    private boolean isProdProfile() {
        String active = System.getenv("SPRING_PROFILES_ACTIVE");
        if (active == null) {
            active = System.getProperty("spring.profiles.active", "");
        }
        return active.contains("prod");
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair jwtSigningKeyPair) {
        var pub = (RSAPublicKey) jwtSigningKeyPair.getPublic();
        var priv = (RSAPrivateKey) jwtSigningKeyPair.getPrivate();
        String kid = authProperties.getJwt().getKeyId();
        return NimbusJwtEncoder.withKeyPair(pub, priv)
                .jwkPostProcessor(builder -> builder.keyID(kid))
                .algorithm(SignatureAlgorithm.RS256)
                .build();
    }

    public static KeyPair loadPkcs8RsaFromPem(String pem)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String normalized =
                pem.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
        byte[] pkcs8 = Base64.getDecoder().decode(normalized);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        var privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        if (!(privateKey instanceof java.security.interfaces.RSAPrivateCrtKey crt)) {
            throw new InvalidKeySpecException(
                    "Private key must be a standard PKCS#8 RSA key (CRT fields required).");
        }
        var publicSpec = new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
        var publicKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);
        return new KeyPair(publicKey, privateKey);
    }
}
