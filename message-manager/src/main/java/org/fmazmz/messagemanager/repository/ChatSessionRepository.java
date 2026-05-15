package org.fmazmz.messagemanager.repository;

import org.fmazmz.messagemanager.model.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {
}
