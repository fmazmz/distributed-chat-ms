package org.fmazmz.messagemanager.service;

import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.platform.kafka.event.MessageSentEvent;
import org.fmazmz.messagemanager.exception.ChatSessionAccessDeniedException;
import org.fmazmz.messagemanager.exception.ChatSessionNotFoundException;
import org.fmazmz.messagemanager.exception.MessageOperationException;
import org.fmazmz.messagemanager.model.ChatSessionEntity;
import org.fmazmz.messagemanager.model.ChatStatus;
import org.fmazmz.messagemanager.model.Message;
import org.fmazmz.messagemanager.repository.ChatSessionRepository;
import org.fmazmz.messagemanager.repository.MessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageApplicationService {

    private final MessageRepository messageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserProfilePort userProfilePort;

    @Transactional
    public Message persistAndPublishChatMessage(UUID sessionId, UUID senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new MessageOperationException("Message body is empty");
        }
        ChatSessionEntity session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ChatSessionNotFoundException(sessionId));
        if (session.getStatus() != ChatStatus.ACTIVE) {
            throw new MessageOperationException("Chat session is not active");
        }
        if (!session.hasParticipant(senderId)) {
            throw new ChatSessionAccessDeniedException(sessionId, senderId);
        }
        userProfilePort.assertUserExists(senderId);

        Message message = new Message();
        message.setChatSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);

        Instant beforeSave = Instant.now();
        Message saved = messageRepository.save(message);
        Instant createdAt = saved.getCreatedAt() != null ? saved.getCreatedAt() : beforeSave;

        applicationEventPublisher.publishEvent(
                new MessageSentEvent(saved.getId(), sessionId, senderId, createdAt, content.length()));

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Message> listMessagesForParticipant(UUID chatSessionId, UUID userId) {
        ChatSessionEntity session = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatSessionNotFoundException(chatSessionId));
        if (!session.hasParticipant(userId)) {
            throw new ChatSessionAccessDeniedException(chatSessionId, userId);
        }
        return messageRepository.findByChatSessionIdOrderByCreatedAtAsc(chatSessionId);
    }

    /**
     * Erases persisted messages + session metadata for GDPR-style cleanup (invoked after user confirms in UI).
     */
    @Transactional
    public void purgeSessionForUser(UUID sessionId, UUID userId) {
        ChatSessionEntity session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ChatSessionNotFoundException(sessionId));
        if (!session.hasParticipant(userId)) {
            throw new ChatSessionAccessDeniedException(sessionId, userId);
        }
        messageRepository.deleteAllByChatSessionId(sessionId);
        chatSessionRepository.deleteById(sessionId);
    }
}
