package org.fmazmz.userservice.user;

import org.fmazmz.userservice.user.api.dto.NewUserRequest;
import org.fmazmz.userservice.user.api.dto.NewUserResponse;
import org.fmazmz.userservice.user.api.dto.UserDetailsResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(NewUserRequest req) {
        User user = new User();
        user.setUserName(req.userName());
        user.setEmail(req.email());
        return user;
    }

    public NewUserResponse toDto(User user) {
        return new NewUserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail()
        );
    }

    public UserDetailsResponse toUserDetailsDto(User user) {
        return new UserDetailsResponse(user.getUserName());
    }
}
