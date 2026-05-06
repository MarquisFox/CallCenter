package ru.vinpin.statisticservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.vinpin.statisticservice.dto.api.CallDto;
import ru.vinpin.statisticservice.services.CallService;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;

    @Operation(summary = "Получить список звонков с пагинацией")
    @GetMapping("/get-all-calls")
    public Page<CallDto> getAllCalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return callService.getAllCalls(pageable);
    }
}