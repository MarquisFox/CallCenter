package ru.vinpin.statisticservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.vinpin.statisticservice.dto.api.DashboardAggregatedDto;
import ru.vinpin.statisticservice.services.DashboardService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "Получить агрегированные данные для дашборда")
    @GetMapping("/aggregated")
    public DashboardAggregatedDto getAggregated(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> managerIds) {

        return dashboardService.getAggregatedData(startDate, endDate, managerIds);
    }
}
