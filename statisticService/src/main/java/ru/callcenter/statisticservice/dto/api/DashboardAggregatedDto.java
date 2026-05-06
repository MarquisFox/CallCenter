package ru.vinpin.statisticservice.dto.api;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class DashboardAggregatedDto {
    private Map<String, Integer> callsByDay;
    private Map<String, Integer> durationByDay;
    private Map<Long, Integer> callsByManager;
    private Map<Long, BigDecimal> errorRateByManager;
}