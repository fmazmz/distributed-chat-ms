package org.fmazmz.analyticssvc.application;

import lombok.RequiredArgsConstructor;
import org.fmazmz.analyticssvc.model.DailyStat;
import org.fmazmz.analyticssvc.repository.DailyStatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyStatService {

    private final DailyStatRepository repository;

    public List<DailyStat> findAll() {
        return repository.findAll();
    }

    public DailyStat findByDate(LocalDate date) {
        return repository.findByDate(date).orElseThrow(() ->
                new IllegalArgumentException("No records found for this date")
        );
    }

    public DailyStat incrementToday() {

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        DailyStat stat = repository.findByDate(today)
                .orElse(new DailyStat(today, 0));

        stat.setMessageCount(stat.getMessageCount() + 1);

        return repository.save(stat);
    }

    public void create() {
        DailyStat stat = new DailyStat(LocalDate.now(ZoneOffset.UTC), 1);
        repository.saveAndFlush(stat);
    }

    public void handleNew() {
        if (!repository.existsByDate(LocalDate.now(ZoneOffset.UTC))) {
            create();
        }
        incrementToday();
    }
}