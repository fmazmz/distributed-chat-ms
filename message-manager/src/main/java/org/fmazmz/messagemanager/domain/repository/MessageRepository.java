package org.fmazmz.messagemanager.domain.repository;

import org.fmazmz.messagemanager.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByRoom_IdOrderByCreatedAtAsc(UUID roomId, Pageable pageable);
}
