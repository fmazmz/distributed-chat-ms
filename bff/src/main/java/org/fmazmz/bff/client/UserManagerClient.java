package org.fmazmz.bff.client;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.bff.config.BffProperties;
import org.fmazmz.bff.dto.user.ApiResponse;
import org.fmazmz.bff.dto.user.NewUserRequest;
import org.fmazmz.bff.dto.user.NewUserResponse;
import org.fmazmz.bff.dto.user.UserDetailsResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class UserManagerClient {

    private final RestClient restClient;
    private final String baseUrl;

    public UserManagerClient(BffProperties properties) {
        this.baseUrl = properties.getUserManagerUrl();
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        log.info("UserManagerClient -> {}", baseUrl);
    }

    public NewUserResponse createUser(NewUserRequest request, String accessToken) {
        log.info("user-manager POST /api/v1/users id={} at {}", request.id(), baseUrl);
        try {
            ApiResponse<NewUserResponse> wrapped = restClient
                    .post()
                    .uri("/api/v1/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return wrapped != null ? wrapped.data() : null;
        } catch (RestClientResponseException ex) {
            log.error(
                    "user-manager createUser failed: url={} status={} body={}",
                    baseUrl,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw ex;
        }
    }

    public UserDetailsResponse getUserDetails(UUID id, String accessToken) {
        ApiResponse<UserDetailsResponse> wrapped = restClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/users/details").queryParam("id", id).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return wrapped != null ? wrapped.data() : null;
    }
}
