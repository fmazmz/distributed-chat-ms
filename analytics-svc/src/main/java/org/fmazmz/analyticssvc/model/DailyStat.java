package org.fmazmz.analyticssvc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_stat")
@Getter
@Setter
public class DailyStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int messageCount;

    @CreationTimestamp
    private LocalDate date;
}
