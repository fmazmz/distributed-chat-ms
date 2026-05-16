package org.fmazmz.messagemanager.adapter.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.model.Message;
import org.fmazmz.messagemanager.security.JwtUtils;
import org.fmazmz.messagemanager.service.MessageApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/sessions/{sessionId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService messageApplicationService;
    private final JwtUtils jwtUtils;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(
            @PathVariable UUID sessionId, @Valid @RequestBody SendMessageRequest body, @AuthenticationPrincipal Jwt jwt) {
        UUID senderId = jwtUtils.subjectAsUuid(jwt);
        Message saved = messageApplicationService.persistAndPublishChatMessage(sessionId, senderId, body.content());
        return MessageResponse.from(saved);
    }

    @GetMapping
    public List<MessageResponse> list(@PathVariable UUID sessionId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.subjectAsUuid(jwt);
        return messageApplicationService.listMessagesForParticipant(sessionId, userId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    public record SendMessageRequest(@NotBlank String content) {}

    public record MessageResponse(UUID id, UUID chatSessionId, UUID senderId, String content, java.time.Instant createdAt) {
        static MessageResponse from(Message message) {
            return new MessageResponse(
                    message.getId(),
                    message.getChatSessionId(),
                    message.getSenderId(),
                    message.getContent(),
                    message.getCreatedAt());
        }
    }
}
