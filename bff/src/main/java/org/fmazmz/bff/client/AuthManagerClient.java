package org.fmazmz.bff.client;

import lombok.extern.slf4j.Slf4j;
import org.fmazmz.bff.config.BffProperties;
import org.fmazmz.bff.dto.auth.TokenResponse;
import org.fmazmz.bff.dto.auth.WebAuthnFinishRequest;
import org.fmazmz.bff.dto.auth.WebAuthnLoginStartRequest;
import org.fmazmz.bff.dto.auth.WebAuthnLoginStartResponse;
import org.fmazmz.bff.dto.auth.WebAuthnRegistrationStartRequest;
import org.fmazmz.bff.dto.auth.WebAuthnRegistrationStartResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class AuthManagerClient {

    private final RestClient restClient;

    public AuthManagerClient(BffProperties properties) {
        String baseUrl = properties.getAuthManagerUrl();
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        log.info("AuthManagerClient -> {}", baseUrl);
    }

    public WebAuthnRegistrationStartResponse registerStart(WebAuthnRegistrationStartRequest body) {
        try {
            return restClient
                    .post()
                    .uri("/api/v1/auth/webauthn/register/start")
                    .body(body)
                    .retrieve()
                    .body(WebAuthnRegistrationStartResponse.class);
        } catch (RestClientResponseException ex) {
            log.error(
                    "auth-manager register/start failed: status={} body={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw ex;
        }
    }

    public TokenResponse registerFinish(WebAuthnFinishRequest body) {
        try {
            return restClient
                    .post()
                    .uri("/api/v1/auth/webauthn/register/finish")
                    .body(body)
                    .retrieve()
                    .body(TokenResponse.class);
        } catch (RestClientResponseException ex) {
            log.error(
                    "auth-manager register/finish failed: status={} body={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw ex;
        }
    }

    public WebAuthnLoginStartResponse loginStart(WebAuthnLoginStartRequest body) {
        return restClient
                .post()
                .uri("/api/v1/auth/webauthn/login/start")
                .body(body)
                .retrieve()
                .body(WebAuthnLoginStartResponse.class);
    }

    public TokenResponse loginFinish(WebAuthnFinishRequest body) {
        return restClient
                .post()
                .uri("/api/v1/auth/webauthn/login/finish")
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }
}
