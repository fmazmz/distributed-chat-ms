package org.fmazmz.messagemanager.adapter.web.session;

import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.service.MessageApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Privacy: user-confirmed wipe of all persisted data for a chat session they belong to.
 */
@RestController
@RequestMapping("/api/chat-sessions")
@RequiredArgsConstructor
public class ChatSessionPrivacyController {

    private final MessageApplicationService messageApplicationService;

    @DeleteMapping("/{sessionId}/data")
    public ResponseEntity<Void> deleteSessionAndMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageApplicationService.purgeSessionForUser(sessionId, userId);
        return ResponseEntity.noContent().build();
    }
}
