package org.fmazmz.authmanager.repository;

import java.util.Optional;
import java.util.UUID;
import org.fmazmz.authmanager.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedIsFalse(String tokenHash);
}
