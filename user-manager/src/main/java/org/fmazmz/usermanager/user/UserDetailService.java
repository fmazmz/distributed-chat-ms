package org.fmazmz.usermanager.user;

import org.fmazmz.usermanager.user.web.dto.NewUserRequest;
import org.fmazmz.usermanager.user.web.dto.NewUserResponse;
import org.fmazmz.usermanager.user.web.dto.UserDetailsRequest;
import org.fmazmz.usermanager.user.web.dto.UserDetailsResponse;
import org.fmazmz.usermanager.user.exception.DuplicateUserException;
import org.fmazmz.usermanager.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
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

    @Transactional
    public NewUserResponse createUser(NewUserRequest request) {
        if (repository.existsById(request.id())) {
            User existing = repository
                    .findById(request.id())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.id()));
            if (existing.getUserName().equalsIgnoreCase(request.userName())
                    && existing.getEmail().equalsIgnoreCase(request.email())) {
                log.info("Profile already exists for id={}, returning existing", request.id());
                return mapper.toDto(existing);
            }
            throw new DuplicateUserException("A user with this id already exists.");
        }
        validator.validateNewUser(request);
        User newUser = mapper.toEntity(request);
        User saved = repository.save(newUser);
        log.info("Created profile id={} userName={}", saved.getId(), saved.getUserName());
        return mapper.toDto(saved);
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
