package org.fmazmz.messagemanager.application.room;

import org.fmazmz.messagemanager.application.room.dto.CreateRoomRequest;
import org.fmazmz.messagemanager.application.room.dto.RoomResponse;
import org.fmazmz.messagemanager.domain.exception.RoomNotFoundException;
import org.fmazmz.messagemanager.domain.repository.RoomRepository;
import org.fmazmz.messagemanager.domain.model.Room;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoomApplicationService {

    private final RoomRepository roomRepository;

    public RoomApplicationService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public RoomResponse createRoom(CreateRoomRequest request) {
        Room room = new Room();
        room.setName(request.name());
        roomRepository.saveAndFlush(room);
        return RoomResponse.from(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms() {
        return roomRepository.findAll().stream().map(RoomResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(UUID id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException(id));
        return RoomResponse.from(room);
    }
}
