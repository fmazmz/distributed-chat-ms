package org.fmazmz.analyticssvc.application;

import lombok.extern.slf4j.Slf4j;
import org.fmazmz.analyticssvc.exception.DuplicateEventException;
import org.fmazmz.analyticssvc.model.MessageSentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class EventHandler {

    private final MessageEventService messageEventService;

    public EventHandler(MessageEventService messageEventService) {
        this.messageEventService = messageEventService;
    }

    @Transactional
    @KafkaListener(topics = "message-published")
    public void onEventReceived(MessageSentEvent event) {
        try {
            messageEventService.process(event);
        } catch (DuplicateEventException ignored) {
        } catch (Exception e) {
            log.error("Failed processing eventId={}", event.getEventId(), e);
            throw e;
        }
    }
}
