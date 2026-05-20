package org.fmazmz.authmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "webauthn_credentials",
        uniqueConstraints = @UniqueConstraint(name = "uk_webauthn_credential_id", columnNames = "credential_id"))
@Getter
@Setter
public class WebAuthnCredential {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    /** Raw credential ID bytes from the authenticator. */
    @Column(name = "credential_id", nullable = false, columnDefinition = "bytea")
    private byte[] credentialId;

    /** WebAuthn user handle (stable byte sequence); we derive from {@code userId} UTF-8. */
    @Column(name = "user_handle", nullable = false, columnDefinition = "bytea")
    private byte[] userHandle;

    @Column(name = "public_key_cose", nullable = false, columnDefinition = "bytea")
    private byte[] publicKeyCose;

    @Column(name = "signature_count", nullable = false)
    private long signatureCount;
}
