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

        private String issuer = "auth-manager";

        private String keyId = "auth-manager-key-1";
        private Duration accessTokenTtl = Duration.ofHours(8);
        private final Rsa rsa = new Rsa();
    }

    @Getter
    @Setter
    public static class Rsa {

        private String privateKeyPem = "";

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
