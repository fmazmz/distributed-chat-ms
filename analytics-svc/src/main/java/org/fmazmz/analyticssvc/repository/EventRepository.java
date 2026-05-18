package org.fmazmz.analyticssvc.repository;

import org.fmazmz.analyticssvc.model.MessageSentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<MessageSentEvent, UUID> {
}
