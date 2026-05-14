package org.fmazmz.authmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.api.TokenResponse;
import org.fmazmz.authmanager.api.WebAuthnLoginStartResponse;
import org.fmazmz.authmanager.api.WebAuthnRegistrationStartResponse;
import org.fmazmz.authmanager.config.AuthProperties;
import org.fmazmz.authmanager.domain.WebAuthnCeremony;
import org.fmazmz.authmanager.domain.WebAuthnCeremony.Kind;
import org.fmazmz.authmanager.domain.WebAuthnCredential;
import org.fmazmz.authmanager.repository.WebAuthnCeremonyRepository;
import org.fmazmz.authmanager.repository.WebAuthnCredentialRepository;
import org.fmazmz.authmanager.webauthn.JpaCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebAuthnCeremonyService {

    private final RelyingParty relyingParty;
    private final AuthProperties authProperties;
    private final WebAuthnCeremonyRepository ceremonyRepository;
    private final WebAuthnCredentialRepository credentialRepository;
    private final TokenService tokenService;

    @Transactional
    public WebAuthnRegistrationStartResponse startRegistration(String userId, String displayName)
            throws JsonProcessingException {
        var handle = JpaCredentialRepository.userHandleForUserId(userId);
        UserIdentity user =
                UserIdentity.builder().name(userId).displayName(displayName).id(handle).build();
        var authenticatorSelection = AuthenticatorSelectionCriteria.builder()
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build();
        var options = StartRegistrationOptions.builder()
                .user(user)
                .authenticatorSelection(authenticatorSelection)
                .build();
        PublicKeyCredentialCreationOptions creationOptions = relyingParty.startRegistration(options);

        WebAuthnCeremony ceremony = new WebAuthnCeremony();
        ceremony.setId(UUID.randomUUID());
        ceremony.setKind(Kind.REGISTRATION);
        ceremony.setUserId(userId);
        ceremony.setRequestJson(creationOptions.toJson());
        ceremony.setExpiresAt(Instant.now().plus(authProperties.getWebauthn().getCeremonyTtl()));
        ceremonyRepository.save(ceremony);
        return new WebAuthnRegistrationStartResponse(
                ceremony.getId().toString(), creationOptions.toCredentialsCreateJson());
    }

    @Transactional
    public TokenResponse finishRegistration(String ceremonyId, String credentialJson)
            throws RegistrationFailedException, IOException {
        WebAuthnCeremony ceremony = loadCeremony(ceremonyId, Kind.REGISTRATION);
        PublicKeyCredentialCreationOptions creationOptions =
                PublicKeyCredentialCreationOptions.fromJson(ceremony.getRequestJson());
        if (!creationOptions.getUser().getName().equals(ceremony.getUserId())) {
            throw new IllegalArgumentException("Ceremony user mismatch");
        }
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response =
                PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
        var result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                .request(creationOptions)
                .response(response)
                .build());

        WebAuthnCredential entity = new WebAuthnCredential();
        entity.setUserId(ceremony.getUserId());
        entity.setCredentialId(result.getKeyId().getId().getBytes());
        entity.setUserHandle(creationOptions.getUser().getId().getBytes());
        entity.setPublicKeyCose(result.getPublicKeyCose().getBytes());
        entity.setSignatureCount(result.getSignatureCount());
        credentialRepository.save(entity);

        ceremonyRepository.delete(ceremony);
        return tokenService.issueForUser(ceremony.getUserId());
    }

    @Transactional
    public WebAuthnLoginStartResponse startLogin(String userId) throws JsonProcessingException {
        JpaCredentialRepository.userHandleForUserId(userId);
        var options = StartAssertionOptions.builder()
                .username(userId)
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build();
        AssertionRequest request = relyingParty.startAssertion(options);

        WebAuthnCeremony ceremony = new WebAuthnCeremony();
        ceremony.setId(UUID.randomUUID());
        ceremony.setKind(Kind.ASSERTION);
        ceremony.setUserId(userId);
        ceremony.setRequestJson(request.toJson());
        ceremony.setExpiresAt(Instant.now().plus(authProperties.getWebauthn().getCeremonyTtl()));
        ceremonyRepository.save(ceremony);
        return new WebAuthnLoginStartResponse(ceremony.getId().toString(), request.toCredentialsGetJson());
    }

    @Transactional
    public TokenResponse finishLogin(String ceremonyId, String credentialJson)
            throws AssertionFailedException, IOException {
        WebAuthnCeremony ceremony = loadCeremony(ceremonyId, Kind.ASSERTION);
        AssertionRequest request = AssertionRequest.fromJson(ceremony.getRequestJson());
        if (!request.getUsername().orElse("").equals(ceremony.getUserId())) {
            throw new IllegalArgumentException("Ceremony user mismatch");
        }
        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> response =
                PublicKeyCredential.parseAssertionResponseJson(credentialJson);
        var result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                .request(request)
                .response(response)
                .build());

        WebAuthnCredential entity = credentialRepository
                .findByCredentialId(result.getCredentialId().getBytes())
                .orElseThrow(() -> new IllegalStateException("Credential not found after successful assertion"));
        entity.setSignatureCount(result.getSignatureCount());
        credentialRepository.save(entity);

        ceremonyRepository.delete(ceremony);
        return tokenService.issueForUser(ceremony.getUserId());
    }

    private WebAuthnCeremony loadCeremony(String ceremonyId, Kind expectedKind) {
        WebAuthnCeremony ceremony = ceremonyRepository
                .findById(UUID.fromString(ceremonyId))
                .orElseThrow(() -> new IllegalArgumentException("Unknown ceremony"));
        if (!ceremony.getKind().equals(expectedKind)) {
            throw new IllegalArgumentException("Wrong ceremony type");
        }
        if (ceremony.getExpiresAt().isBefore(Instant.now())) {
            ceremonyRepository.delete(ceremony);
            throw new IllegalArgumentException("Ceremony expired");
        }
        return ceremony;
    }
}
