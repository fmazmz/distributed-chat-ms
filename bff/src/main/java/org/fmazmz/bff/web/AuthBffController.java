package org.fmazmz.bff.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.bff.security.ChatAccessTokenCookie;
import org.fmazmz.bff.client.AuthManagerClient;
import org.fmazmz.bff.client.UserManagerClient;
import org.fmazmz.bff.dto.auth.RegisterFinishBffRequest;
import org.fmazmz.bff.dto.auth.TokenResponse;
import org.fmazmz.bff.dto.auth.WebAuthnFinishRequest;
import org.fmazmz.bff.dto.auth.WebAuthnLoginStartRequest;
import org.fmazmz.bff.dto.auth.WebAuthnLoginStartResponse;
import org.fmazmz.bff.dto.auth.WebAuthnRegistrationStartRequest;
import org.fmazmz.bff.dto.auth.WebAuthnRegistrationStartResponse;
import org.fmazmz.bff.dto.user.NewUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthBffController {

    private final AuthManagerClient authManagerClient;
    private final UserManagerClient userManagerClient;

    @PostMapping("/register/start")
    public WebAuthnRegistrationStartResponse registerStart(@Valid @RequestBody WebAuthnRegistrationStartRequest body) {
        log.info("register/start: userName={}", body.userName());
        return authManagerClient.registerStart(body);
    }

    @PostMapping("/register/finish")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registerFinish(@Valid @RequestBody RegisterFinishBffRequest body) {
        log.info("register/finish: userName={}, ceremonyId={}", body.userName(), body.ceremonyId());
        TokenResponse tokens = authManagerClient.registerFinish(
                new WebAuthnFinishRequest(body.ceremonyId(), body.credentialJson()));
        userManagerClient.createUser(
                new NewUserRequest(UUID.fromString(tokens.userId()), body.userName(), body.email()),
                tokens.accessToken());
        log.info("register/finish complete: userId={}", tokens.userId());
        return tokens;
    }

    @PostMapping("/login/start")
    public WebAuthnLoginStartResponse loginStart(@Valid @RequestBody WebAuthnLoginStartRequest body) {
        return authManagerClient.loginStart(body);
    }

    @PostMapping("/login/finish")
    public TokenResponse loginFinish(@Valid @RequestBody WebAuthnFinishRequest body) {
        return authManagerClient.loginFinish(body);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        ChatAccessTokenCookie.clear(response, request.isSecure());
    }
}
