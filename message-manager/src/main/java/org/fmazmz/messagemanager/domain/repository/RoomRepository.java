package org.fmazmz.messagemanager.domain.repository;

import org.fmazmz.messagemanager.domain.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
}
