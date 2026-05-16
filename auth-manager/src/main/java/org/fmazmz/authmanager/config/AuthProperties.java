package org.fmazmz.authmanager.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AuthProperties {

    private final Jwt jwt = new Jwt();
    private final WebAuthn webauthn = new WebAuthn();

    @Getter
    @Setter
    public static class Jwt {
        /** Must match what resource servers expect in the `iss` claim. */
        private String issuer = "auth-manager";
        /** `kid` in JWT header and JWKS; use a new id when rotating keys. */
        private String keyId = "auth-manager-key-1";
        private Duration accessTokenTtl = Duration.ofHours(8);
        private final Rsa rsa = new Rsa();
    }

    @Getter
    @Setter
    public static class Rsa {
        /**
         * PKCS#8 PEM ({@code -----BEGIN PRIVATE KEY-----}). Set via env {@code JWT_RSA_PRIVATE_KEY}
         * (e.g. from {@code .env}; use literal newlines or escaped {@code \n} in a single line).
         */
        private String privateKeyPem = "";
        /**
         * Alternative to {@link #privateKeyPem}: base64-encoded UTF-8 PEM (easier for one-line {@code .env}).
         * Env: {@code JWT_RSA_PRIVATE_KEY_B64}. If both are set, this takes precedence.
         */
        private String privateKeyPemBase64 = "";
    }

    @Getter
    @Setter
    public static class WebAuthn {
        private String rpId = "localhost";
        private String rpName = "app";
        private List<String> origins = new ArrayList<>();
        private Duration ceremonyTtl = Duration.ofMinutes(5);
    }
}
