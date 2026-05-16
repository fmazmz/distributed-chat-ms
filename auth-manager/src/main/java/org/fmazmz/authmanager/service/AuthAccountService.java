package org.fmazmz.authmanager.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.domain.AuthAccount;
import org.fmazmz.authmanager.domain.AuthAccount.Status;
import org.fmazmz.authmanager.repository.AuthAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthAccountService {

    private final AuthAccountRepository repository;

    @Transactional
    public AuthAccount createPending(String userName, String email) {
        if (repository.existsByUserNameIgnoreCase(userName)) {
            throw new IllegalArgumentException("Username already registered");
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        AuthAccount account = new AuthAccount();
        account.setId(UUID.randomUUID());
        account.setUserName(userName);
        account.setEmail(email);
        account.setStatus(Status.PENDING);
        account.setCreatedAt(Instant.now());
        return repository.save(account);
    }

    @Transactional(readOnly = true)
    public AuthAccount requireActiveByUserName(String userName) {
        return repository
                .findByUserNameIgnoreCaseAndStatus(userName, Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user or registration incomplete"));
    }

    @Transactional
    public void activate(UUID accountId) {
        AuthAccount account = repository
                .findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setStatus(Status.ACTIVE);
        repository.save(account);
    }
}
