package org.fmazmz.messagemanager.application.room.dto;

import org.fmazmz.messagemanager.domain.model.Room;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(UUID id, String name, Instant createdAt, Instant updatedAt) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCreatedAt(), room.getUpdatedAt());
    }
}
