package org.fmazmz.authmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.fmazmz.authmanager.model.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, UUID> {

    List<WebAuthnCredential> findAllByUserId(String userId);

    Optional<WebAuthnCredential> findByCredentialId(byte[] credentialId);

    List<WebAuthnCredential> findAllByCredentialId(byte[] credentialId);
}
