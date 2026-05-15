package org.fmazmz.usermanager.user.web;

import jakarta.validation.Valid;
import org.fmazmz.usermanager.user.UserDetailService;
import org.fmazmz.usermanager.user.web.dto.NewUserRequest;
import org.fmazmz.usermanager.user.web.dto.NewUserResponse;
import org.fmazmz.usermanager.user.web.dto.UserDetailsRequest;
import org.fmazmz.usermanager.user.web.dto.UserDetailsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        path = "api/v1/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class UserController {
    private final UserDetailService service;

    public UserController(UserDetailService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<NewUserResponse>> newUser(
            @RequestBody@Valid NewUserRequest request) {

        NewUserResponse response = service.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("details")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUser(@RequestParam("id") UUID id) {

        UserDetailsResponse response = service.getUserDetails(new UserDetailsRequest(id));

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
