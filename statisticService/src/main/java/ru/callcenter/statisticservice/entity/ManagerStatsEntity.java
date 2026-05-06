package ru.vinpin.statisticservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "manager_stats")
public class ManagerStatsEntity {
    @Id
    private Long managerId;

    @Column(name = "total_calls", nullable = false)
    private Integer totalCalls;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "avg_error_rate", precision = 5, scale = 4)
    private BigDecimal avgErrorRate;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
}