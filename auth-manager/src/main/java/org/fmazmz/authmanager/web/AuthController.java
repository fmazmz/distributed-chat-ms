package org.fmazmz.authmanager.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.api.RefreshRequest;
import org.fmazmz.authmanager.api.TokenResponse;
import org.fmazmz.authmanager.api.WebAuthnFinishRequest;
import org.fmazmz.authmanager.api.WebAuthnLoginStartRequest;
import org.fmazmz.authmanager.api.WebAuthnLoginStartResponse;
import org.fmazmz.authmanager.api.WebAuthnRegistrationStartRequest;
import org.fmazmz.authmanager.api.WebAuthnRegistrationStartResponse;
import org.fmazmz.authmanager.service.TokenService;
import org.fmazmz.authmanager.service.WebAuthnCeremonyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WebAuthnCeremonyService webAuthnCeremonyService;
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest body) {
        return tokenService.refresh(body.refreshToken());
    }

    @PostMapping("/webauthn/register/start")
    public WebAuthnRegistrationStartResponse webAuthnRegisterStart(
            @Valid @RequestBody WebAuthnRegistrationStartRequest body) throws Exception {
        return webAuthnCeremonyService.startRegistration(body.userId(), body.displayName());
    }

    @PostMapping("/webauthn/register/finish")
    public TokenResponse webAuthnRegisterFinish(@Valid @RequestBody WebAuthnFinishRequest body) throws Exception {
        return webAuthnCeremonyService.finishRegistration(body.ceremonyId(), body.credentialJson());
    }

    @PostMapping("/webauthn/login/start")
    public WebAuthnLoginStartResponse webAuthnLoginStart(@Valid @RequestBody WebAuthnLoginStartRequest body)
            throws Exception {
        return webAuthnCeremonyService.startLogin(body.userId());
    }

    @PostMapping("/webauthn/login/finish")
    public TokenResponse webAuthnLoginFinish(@Valid @RequestBody WebAuthnFinishRequest body) throws Exception {
        return webAuthnCeremonyService.finishLogin(body.ceremonyId(), body.credentialJson());
    }
}
