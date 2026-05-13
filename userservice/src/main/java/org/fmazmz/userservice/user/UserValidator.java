package org.fmazmz.userservice.user;

import org.fmazmz.userservice.user.api.dto.NewUserRequest;
import org.fmazmz.userservice.user.exception.DuplicateUserException;
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
