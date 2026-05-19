package org.fmazmz.analyticssvc.repository;

import org.fmazmz.analyticssvc.model.DailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface DailyStatRepository extends JpaRepository<DailyStat, UUID> {
    boolean existsByDate(LocalDate date);
    DailyStat findByDate(LocalDate date);
}
