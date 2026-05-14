package org.fmazmz.messagemanager.service;

import org.fmazmz.messagemanager.adapter.web.message.dto.CreateMessageRequest;
import org.fmazmz.messagemanager.adapter.web.message.dto.MessageResponse;
import org.fmazmz.messagemanager.repository.MessageRepository;
import org.fmazmz.messagemanager.model.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MessageApplicationService {

    private final MessageRepository messageRepository;
    private final UserProfilePort userProfilePort;

    public MessageApplicationService(
            MessageRepository messageRepository,
            UserProfilePort userProfilePort) {
        this.messageRepository = messageRepository;
        this.userProfilePort = userProfilePort;
    }

    public MessageResponse postMessage(UUID roomId, CreateMessageRequest request) {
        userProfilePort.assertUserExists(request.senderId());
        Message message = new Message();
        message.setSenderId(request.senderId());
        message.setContent(request.content());
        messageRepository.save(message);
        return MessageResponse.from(message);
    }
}
