package ru.vinpin.bitrixadapterservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.vinpin.bitrixadapterservice.dto.CallEventRequest;
import ru.vinpin.bitrixadapterservice.dto.CallEventResponse;
import ru.vinpin.bitrixadapterservice.service.CallEventMapper;
import ru.vinpin.bitrixadapterservice.service.KafkaProducerService;
import ru.vinpin.bitrixadapterservice.service.ProxyService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "API Gateway", description = "Единая точка входа для CRM и фронта")
public class GatewayController {

    private final CallEventMapper callEventMapper;
    private final KafkaProducerService kafkaProducer;
    private final ProxyService proxyService;

    @Operation(
            summary = "Добавить информацию о звонке от CRM",
            description = "Принимает метаданные звонка, валидирует, нормализует и отправляет в Kafka (топик call.registration). "
                    + "Возвращает 202 Accepted при успешной постановке в очередь."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Сообщение принято и отправлено в Kafka",
                    content = @Content(schema = @Schema(implementation = CallEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации: отсутствуют обязательные поля (callId, managerId, fileUrl)"),
            @ApiResponse(responseCode = "503", description = "Сервис Kafka недоступен")
    })
    @PostMapping("/call-events")
    public ResponseEntity<?> createCallEvent(@RequestBody CallEventRequest request) {

        if (request.getCallId() == null || request.getCallId().isBlank() ||
                request.getManagerId() == null || request.getManagerId().isBlank() ||
                request.getFileUrl() == null || request.getFileUrl().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: callId, managerId, fileUrl"));
        }

        try {
            var normalized = callEventMapper.normalizeToKafkaMessage(request);
            String json = callEventMapper.serializeToJson(normalized);
            kafkaProducer.send(request.getCallId(), json);
            return ResponseEntity.accepted()
                    .body(new CallEventResponse("accepted", request.getCallId()));
        } catch (Exception e) {
            log.error("Ошибка отправки в Kafka: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Kafka service unavailable"));
        }
    }

    @Operation(
            summary = "Получить список звонков с пагинацией",
            description = "Прокси-запрос к Statistics Service (Data Service). Возвращает страницу со звонками, "
                    + "содержащую id, дату, имя менеджера, длительность и тональность."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ"),
            @ApiResponse(responseCode = "502", description = "Ошибка проксирования (Statistics Service недоступен)")
    })
    @GetMapping("/calls/get-all-calls")
    public Mono<String> getAllCalls(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return proxyService.proxyGet("/calls/get-all-calls",
                ub -> ub.queryParam("page", page).queryParam("size", size));
    }

    @Operation(
            summary = "Агрегированные данные для дашборда",
            description = "Прокси-запрос к Statistics Service. Возвращает количество звонков и длительность по дням, "
                    + "а также статистику по менеджерам за указанный период."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "502", description = "Ошибка проксирования")
    })
    @GetMapping("/dashboard/aggregated")
    public Mono<String> getDashboardAggregated(
            @Parameter(description = "Начальная дата (ISO, например 2025-01-01)")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Конечная дата")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Список ID менеджеров через запятую")
            @RequestParam(required = false) List<Long> managerIds) {
        return proxyService.proxyGet("/dashboard/aggregated",
                ub -> {
                    if (startDate != null) ub.queryParam("startDate", startDate);
                    if (endDate != null) ub.queryParam("endDate", endDate);
                    if (managerIds != null) ub.queryParam("managerIds", managerIds);
                });
    }

    @Operation(summary = "Список менеджеров с процентом ошибок")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    @GetMapping("/managers/get-all-managers-with-error-rate")
    public Mono<String> getAllManagersWithErrorRate() {
        return proxyService.proxyGet("/managers/get-all-managers-with-error-rate", null);
    }

    @Operation(summary = "Детальная информация о менеджере с фильтрацией")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Успешно"))
    @GetMapping("/managers/get-manager-details")
    public Mono<String> getManagerDetails(
            @Parameter(description = "ID менеджера", required = true, example = "1")
            @RequestParam Long id,
            @Parameter(description = "Начальная дата")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Конечная дата")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Список ID тональностей через запятую (1-положительная,2-отрицательная,3-нейтральная)")
            @RequestParam(required = false) String tonalityIds) {
        return proxyService.proxyGet("/managers/get-manager-details",
                ub -> {
                    ub.queryParam("id", id);
                    if (startDate != null) ub.queryParam("startDate", startDate);
                    if (endDate != null) ub.queryParam("endDate", endDate);
                    if (tonalityIds != null) ub.queryParam("tonalityIds", tonalityIds);
                });
    }
}