package org.fmazmz.messagemanager.repository;

import org.fmazmz.messagemanager.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByRoom_IdOrderByCreatedAtAsc(UUID roomId, Pageable pageable);
}
