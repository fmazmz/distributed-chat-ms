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
@Table(name = "auth_accounts")
@Getter
@Setter
public class AuthAccount {

    public enum Status {
        PENDING,
        ACTIVE
    }

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 55)
    private String userName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
