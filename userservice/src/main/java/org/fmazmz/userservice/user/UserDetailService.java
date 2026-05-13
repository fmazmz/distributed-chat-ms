package org.fmazmz.userservice.user;

import org.fmazmz.userservice.user.api.dto.NewUserRequest;
import org.fmazmz.userservice.user.api.dto.NewUserResponse;
import org.fmazmz.userservice.user.api.dto.UserDetailsRequest;
import org.fmazmz.userservice.user.api.dto.UserDetailsResponse;
import org.fmazmz.userservice.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserDetailService {
    private final UserDetailsRepository repository;
    private final UserMapper mapper;
    private final UserValidator validator;

    public UserDetailService(UserDetailsRepository repository, UserMapper mapper, UserValidator validator) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
    }

    public NewUserResponse createUser(NewUserRequest request) {
        validator.validateNewUser(request);
        User newUser = mapper.toEntity(request);
        repository.saveAndFlush(newUser);

        return mapper.toDto(newUser);
    }

    public UserDetailsResponse getUserDetails(UserDetailsRequest request) {
        User user = repository.findById(request.id())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.id()));

        return mapper.toUserDetailsDto(user);
    }

    public User getUserById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}
