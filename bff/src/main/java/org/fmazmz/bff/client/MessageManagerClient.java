package org.fmazmz.bff.client;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.bff.config.BffProperties;
import org.fmazmz.bff.dto.message.MessageResponse;
import org.fmazmz.bff.dto.message.SendMessageRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class MessageManagerClient {

    private final RestClient restClient;

    public MessageManagerClient(BffProperties properties) {
        String baseUrl = properties.getMessageManagerUrl();
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        log.info("MessageManagerClient -> {}", baseUrl);
    }

    public MessageResponse sendMessage(UUID sessionId, SendMessageRequest body, String bearerToken) {
        return restClient
                .post()
                .uri("/api/v1/sessions/{sessionId}/messages", sessionId)
                .header("Authorization", bearerToken)
                .body(body)
                .retrieve()
                .body(MessageResponse.class);
    }

    public List<MessageResponse> listMessages(UUID sessionId, String bearerToken) {
        return restClient
                .get()
                .uri("/api/v1/sessions/{sessionId}/messages", sessionId)
                .header("Authorization", bearerToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
