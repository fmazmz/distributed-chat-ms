package org.fmazmz.authmanager.adapter.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.adapter.api.TokenResponse;
import org.fmazmz.authmanager.adapter.api.WebAuthnFinishRequest;
import org.fmazmz.authmanager.adapter.api.WebAuthnLoginStartRequest;
import org.fmazmz.authmanager.adapter.api.WebAuthnLoginStartResponse;
import org.fmazmz.authmanager.adapter.api.WebAuthnRegistrationStartRequest;
import org.fmazmz.authmanager.adapter.api.WebAuthnRegistrationStartResponse;
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

    @PostMapping("/webauthn/register/start")
    public WebAuthnRegistrationStartResponse webAuthnRegisterStart(
            @Valid @RequestBody WebAuthnRegistrationStartRequest body) throws Exception {
        return webAuthnCeremonyService.startRegistration(body.userName(), body.email());
    }

    @PostMapping("/webauthn/register/finish")
    public TokenResponse webAuthnRegisterFinish(@Valid @RequestBody WebAuthnFinishRequest body) throws Exception {
        return webAuthnCeremonyService.finishRegistration(body.ceremonyId(), body.credentialJson());
    }

    @PostMapping("/webauthn/login/start")
    public WebAuthnLoginStartResponse webAuthnLoginStart(@Valid @RequestBody WebAuthnLoginStartRequest body)
            throws Exception {
        return webAuthnCeremonyService.startLogin(body.userName());
    }

    @PostMapping("/webauthn/login/finish")
    public TokenResponse webAuthnLoginFinish(@Valid @RequestBody WebAuthnFinishRequest body) throws Exception {
        return webAuthnCeremonyService.finishLogin(body.ceremonyId(), body.credentialJson());
    }
}
