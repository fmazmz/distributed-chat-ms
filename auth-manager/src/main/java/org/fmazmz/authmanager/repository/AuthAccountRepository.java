package org.fmazmz.authmanager.repository;

import java.util.Optional;
import java.util.UUID;
import org.fmazmz.authmanager.domain.AuthAccount;
import org.fmazmz.authmanager.domain.AuthAccount.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, UUID> {

    boolean existsByUserNameIgnoreCase(String userName);

    boolean existsByEmailIgnoreCase(String email);

    Optional<AuthAccount> findByUserNameIgnoreCaseAndStatus(String userName, Status status);
}
