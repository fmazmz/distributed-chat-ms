package org.fmazmz.analyticssvc.application;

import org.fmazmz.analyticssvc.exception.DuplicateEventException;
import org.fmazmz.analyticssvc.model.DailyStat;
import org.fmazmz.analyticssvc.model.MessageSentEvent;
import org.fmazmz.analyticssvc.repository.DailyStatRepository;
import org.fmazmz.analyticssvc.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class MessageEventService {
    private final DailyStatRepository dailyStatRepository;
    private final EventRepository eventRepository;

    public MessageEventService(DailyStatRepository dailyStatRepository, EventRepository eventRepository) {
        this.dailyStatRepository = dailyStatRepository;
        this.eventRepository = eventRepository;
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

        DailyStat stat = dailyStatRepository.findByDate(today);

        if (stat == null) {
            stat = new DailyStat();
            stat.setDate(today);
            stat.setMessageCount(0);
        }

        stat.setMessageCount(stat.getMessageCount() + 1);

        dailyStatRepository.save(stat);
        eventRepository.save(event);
    }
}
