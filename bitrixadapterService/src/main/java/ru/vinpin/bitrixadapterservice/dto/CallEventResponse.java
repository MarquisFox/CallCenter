package ru.vinpin.bitrixadapterservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Ответ при успешном принятии звонка")
public class CallEventResponse {
    @Schema(description = "Статус операции", example = "accepted")
    private String status;
    @Schema(description = "Идентификатор звонка", example = "call_123")
    private String callId;
}
