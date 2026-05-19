package org.fmazmz.analyticssvc.application;

import org.fmazmz.analyticssvc.exception.DuplicateEventException;
import org.fmazmz.analyticssvc.model.MessageSentEvent;
import org.fmazmz.analyticssvc.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;


@Service
public class MessageEventService {
    private final EventRepository eventRepository;
    private final DailyStatService dailyStatService;

    public MessageEventService(EventRepository eventRepository, DailyStatService dailyStatService) {
        this.eventRepository = eventRepository;
        this.dailyStatService = dailyStatService;
    }

    @Transactional
    public void process(MessageSentEvent event) {

        boolean exists = eventRepository.existsById(event.getEventId());

        if (exists) {
            throw new DuplicateEventException(
                    "Event already processed: " + event.getEventId()
            );
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        dailyStatService.handleNew();

        eventRepository.saveAndFlush(event);
    }
}
