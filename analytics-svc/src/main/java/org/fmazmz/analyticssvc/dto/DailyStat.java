package org.fmazmz.analyticssvc.dto;

import java.time.LocalDate;

public record DailyStat(
        LocalDate date,
        int messageCount
) {
}
