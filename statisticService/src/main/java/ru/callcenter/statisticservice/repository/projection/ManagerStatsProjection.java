package ru.vinpin.statisticservice.repository.projection;

import java.math.BigDecimal;

public interface ManagerStatsProjection {
    Integer getTotalCalls();
    BigDecimal getAvgRating();
    BigDecimal getAvgErrorRate();
}