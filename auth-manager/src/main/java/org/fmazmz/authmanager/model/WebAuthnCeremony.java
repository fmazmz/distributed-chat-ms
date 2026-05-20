package org.fmazmz.authmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "webauthn_ceremonies")
@Getter
@Setter
public class WebAuthnCeremony {

    public enum Kind {
        REGISTRATION,
        ASSERTION
    }

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Kind kind;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "request_json", nullable = false, columnDefinition = "TEXT")
    private String requestJson;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
