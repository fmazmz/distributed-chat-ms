package org.fmazmz.authmanager.repository;

import java.util.UUID;
import org.fmazmz.authmanager.model.WebAuthnCeremony;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebAuthnCeremonyRepository extends JpaRepository<WebAuthnCeremony, UUID> {}
