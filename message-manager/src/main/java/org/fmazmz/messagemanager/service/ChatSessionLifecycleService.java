package org.fmazmz.messagemanager.service;

import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.exception.ChatSessionAccessDeniedException;
import org.fmazmz.messagemanager.exception.ChatSessionNotFoundException;
import org.fmazmz.messagemanager.model.ChatSessionEntity;
import org.fmazmz.messagemanager.model.ChatStatus;
import org.fmazmz.messagemanager.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionLifecycleService {

    private final ChatSessionRepository chatSessionRepository;

    @Transactional
    public void registerPending(UUID sessionId, UUID requesterId, UUID recipientId) {
        if (chatSessionRepository.existsById(sessionId)) {
            return;
        }
        chatSessionRepository.save(new ChatSessionEntity(sessionId, requesterId, recipientId, ChatStatus.PENDING));
    }

    @Transactional
    public void markActive(UUID sessionId, UUID recipientId) {
        ChatSessionEntity entity = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ChatSessionNotFoundException(sessionId));
        if (!recipientId.equals(entity.getRecipientId())) {
            throw new ChatSessionAccessDeniedException(sessionId, recipientId);
        }
        entity.setStatus(ChatStatus.ACTIVE);
        chatSessionRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId) {
        return chatSessionRepository.findById(sessionId)
                .filter(s -> s.getStatus() == ChatStatus.ACTIVE)
                .isPresent();
    }
}
