package org.fmazmz.messagemanager.application.message;

import org.fmazmz.messagemanager.application.message.dto.CreateMessageRequest;
import org.fmazmz.messagemanager.application.message.dto.MessageResponse;
import org.fmazmz.messagemanager.application.message.dto.MessageSliceResponse;
import org.fmazmz.messagemanager.application.user.UserProfilePort;
import org.fmazmz.messagemanager.domain.exception.RoomNotFoundException;
import org.fmazmz.messagemanager.domain.repository.MessageRepository;
import org.fmazmz.messagemanager.domain.repository.RoomRepository;
import org.fmazmz.messagemanager.domain.model.Message;
import org.fmazmz.messagemanager.domain.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MessageApplicationService {

    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final UserProfilePort userProfilePort;

    public MessageApplicationService(
            RoomRepository roomRepository,
            MessageRepository messageRepository,
            UserProfilePort userProfilePort) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.userProfilePort = userProfilePort;
    }

    public MessageResponse postMessage(UUID roomId, CreateMessageRequest request) {
        userProfilePort.assertUserExists(request.senderId());
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
        Message message = new Message();
        message.setRoom(room);
        message.setSenderId(request.senderId());
        message.setContent(request.content());
        messageRepository.save(message);
        return MessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public MessageSliceResponse listMessages(UUID roomId, Pageable pageable) {
        if (!roomRepository.existsById(roomId)) {
            throw new RoomNotFoundException(roomId);
        }
        Page<Message> page = messageRepository.findByRoom_IdOrderByCreatedAtAsc(roomId, pageable);
        return MessageSliceResponse.from(page);
    }
}
