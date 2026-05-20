package org.fmazmz.analyticssvc.application.graphql;

import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import org.fmazmz.analyticssvc.application.DailyStatService;
import org.fmazmz.analyticssvc.model.DailyStat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class DailyStatDataFetcher {

    private final DailyStatService service;

    @DgsQuery
    public List<DailyStat> dailyStats() {
        return service.findAll()
                .stream()
                .map(e -> new DailyStat(e.getDate(), e.getMessageCount()))
                .toList();
    }

    @DgsQuery
    public DailyStat dailyStat(@InputArgument LocalDate date) {
        var e = service.findByDate(date);
        if (e == null) return null;
        return new DailyStat(e.getDate(), e.getMessageCount());
    }

    @DgsEntityFetcher(name = "DailyStat")
    public DailyStat dailyStatEntity(Map<String, Object> values) {

        String rawDate = (String) values.get("date");
        LocalDate date = LocalDate.parse(rawDate);

        var entity = service.findByDate(date);

        if (entity == null) return null;

        return new DailyStat(entity.getDate(), entity.getMessageCount());
    }
}