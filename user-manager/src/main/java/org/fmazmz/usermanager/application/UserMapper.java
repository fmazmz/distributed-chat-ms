package org.fmazmz.usermanager.application;

import org.fmazmz.usermanager.adapter.web.dto.NewUserRequest;
import org.fmazmz.usermanager.adapter.web.dto.NewUserResponse;
import org.fmazmz.usermanager.adapter.web.dto.UserDetailsResponse;
import org.fmazmz.usermanager.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(NewUserRequest req) {
        User user = new User();
        user.setId(req.id());
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
