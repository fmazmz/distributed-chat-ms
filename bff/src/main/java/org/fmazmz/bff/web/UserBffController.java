package org.fmazmz.bff.web;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.bff.client.UserManagerClient;
import org.fmazmz.bff.config.BffProperties;
import org.fmazmz.bff.dto.user.UserDetailsResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserBffController {

    private final UserManagerClient userManagerClient;
    private final BffProperties bffProperties;

    @GetMapping("/me")
    public UserMeResponse me(@AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        UserDetailsResponse profile = userManagerClient.getUserDetails(id, jwt.getTokenValue());
        return new UserMeResponse(id, profile.userName(), "/ws/chat");
    }

    public record UserMeResponse(UUID id, String userName, String chatWebSocketUrl) {}
}
