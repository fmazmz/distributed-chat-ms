package org.fmazmz.bff.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.bff.client.MessageManagerClient;
import org.fmazmz.bff.dto.message.MessageResponse;
import org.fmazmz.bff.dto.message.SendMessageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
public class MessageBffController {

    private final MessageManagerClient messageManagerClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        return messageManagerClient.sendMessage(sessionId, body, bearer(jwt));
    }

    @GetMapping
    public List<MessageResponse> list(@PathVariable UUID sessionId, @AuthenticationPrincipal Jwt jwt) {
        return messageManagerClient.listMessages(sessionId, bearer(jwt));
    }

    private static String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }
}
