package ru.vinpin.statisticservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.vinpin.statisticservice.dto.api.ManagerDetailsDto;
import ru.vinpin.statisticservice.dto.api.ManagerErrorDto;
import ru.vinpin.statisticservice.services.ManagerService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;

    @Operation(summary = "Получить список всех менеджеров с процентом ошибок")
    @GetMapping("/get-all-managers-with-error-rate")
    public List<ManagerErrorDto> getAllManagersWithErrorRate() {
        return managerService.getAllManagersWithErrorRate();
    }

    @Operation(summary = "Получить детали менеджера с фильтрами")
    @GetMapping("/get-manager-details")
    public ManagerDetailsDto getManagerDetails(
            @RequestParam Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Short> tonalityIds) {

        return managerService.getManagerDetails(id, startDate, endDate, tonalityIds);
    }
}