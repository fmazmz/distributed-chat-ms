package org.fmazmz.usermanager.application;

import org.fmazmz.usermanager.adapter.web.dto.NewUserRequest;
import org.fmazmz.usermanager.exception.DuplicateUserException;
import org.fmazmz.usermanager.repository.UserDetailsRepository;
import org.springframework.stereotype.Service;

@Service
public class UserValidator {
    private final UserDetailsRepository repository;

    public UserValidator(UserDetailsRepository repository) {
        this.repository = repository;
    }

    public void validateNewUser(NewUserRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateUserException("A user with this email already exists.");
        }
        if (repository.existsByUserNameIgnoreCase(request.userName())) {
            throw new DuplicateUserException("A user with this username already exists.");
        }
    }
}
