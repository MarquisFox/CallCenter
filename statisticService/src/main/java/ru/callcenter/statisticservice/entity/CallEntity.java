package ru.vinpin.statisticservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "call")
public class CallEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_name", unique = true, nullable = false)
    private String callName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private ManagerEntity manager;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "duration")
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    private Duration duration;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private StatusEntity status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tonality_id")
    private TonalityEntity tonality;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "error_rate", precision = 5, scale = 4)
    private BigDecimal errorRate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}