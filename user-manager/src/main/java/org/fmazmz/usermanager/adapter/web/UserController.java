package org.fmazmz.usermanager.adapter.web;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.usermanager.application.UserDetailService;
import org.fmazmz.usermanager.adapter.web.dto.NewUserRequest;
import org.fmazmz.usermanager.adapter.web.dto.NewUserResponse;
import org.fmazmz.usermanager.adapter.web.dto.UserDetailsRequest;
import org.fmazmz.usermanager.adapter.web.dto.UserDetailsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(path = "api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserDetailService service;

    public UserController(UserDetailService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<NewUserResponse>> newUser(
            @AuthenticationPrincipal Jwt jwt, @RequestBody @Valid NewUserRequest request) {
        assertTokenMatchesProfile(jwt, request.id());
        log.info("Creating profile id={} userName={}", request.id(), request.userName());
        NewUserResponse response = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("details")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUser(
            @AuthenticationPrincipal Jwt jwt, @RequestParam("id") UUID id) {
        assertTokenMatchesProfile(jwt, id);
        UserDetailsResponse response = service.getUserDetails(new UserDetailsRequest(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private static void assertTokenMatchesProfile(Jwt jwt, UUID profileId) {
        if (!jwt.getSubject().equals(profileId.toString())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "JWT subject must match the requested user id");
        }
    }
}
