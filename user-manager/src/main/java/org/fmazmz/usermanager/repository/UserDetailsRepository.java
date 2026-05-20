package org.fmazmz.usermanager.repository;

import org.fmazmz.usermanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDetailsRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUserNameIgnoreCase(String userName);
}
