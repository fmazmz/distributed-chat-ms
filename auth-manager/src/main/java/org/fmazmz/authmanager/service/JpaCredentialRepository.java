package org.fmazmz.authmanager.service;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.model.WebAuthnCredential;
import org.fmazmz.authmanager.repository.WebAuthnCredentialRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCredentialRepository implements CredentialRepository {

    private final WebAuthnCredentialRepository credentialRepository;

    public static ByteArray userHandleForUserId(String userId) {
        byte[] raw = userId.getBytes(StandardCharsets.UTF_8);
        if (raw.length == 0 || raw.length > 64) {
            throw new IllegalArgumentException("userId must encode to 1..64 bytes for a WebAuthn user handle");
        }
        return new ByteArray(raw);
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return credentialRepository.findAllByUserId(username).stream()
                .map(this::toDescriptor)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return Optional.of(userHandleForUserId(username));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        byte[] bytes = userHandle.getBytes();
        if (bytes.length == 0 || bytes.length > 64) {
            return Optional.empty();
        }
        return Optional.of(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentialRepository
                .findByCredentialId(credentialId.getBytes())
                .filter(entity -> userHandle == null
                        || userHandle.isEmpty()
                        || Arrays.equals(entity.getUserHandle(), userHandle.getBytes()))
                .map(this::toRegistered);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentialRepository.findAllByCredentialId(credentialId.getBytes()).stream()
                .map(this::toRegistered)
                .collect(Collectors.toSet());
    }

    private PublicKeyCredentialDescriptor toDescriptor(WebAuthnCredential entity) {
        return PublicKeyCredentialDescriptor.builder()
                .id(new ByteArray(entity.getCredentialId()))
                .type(PublicKeyCredentialType.PUBLIC_KEY)
                .build();
    }

    private RegisteredCredential toRegistered(WebAuthnCredential entity) {
        return RegisteredCredential.builder()
                .credentialId(new ByteArray(entity.getCredentialId()))
                .userHandle(new ByteArray(entity.getUserHandle()))
                .publicKeyCose(new ByteArray(entity.getPublicKeyCose()))
                .signatureCount(entity.getSignatureCount())
                .build();
    }
}
