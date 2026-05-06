package ru.vinpin.bitrixadapterservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос от CRM с информацией о звонке")
public class CallEventRequest {
    @Schema(description = "Уникальный идентификатор звонка (обязательно)", example = "call_20250408_001")
    @JsonProperty("callId")
    private String callId;
    @Schema(description = "Внешний код менеджера (обязательно)", example = "emp_12345")
    @JsonProperty("managerId")
    private String managerId;
    @Schema(description = "Имя менеджера (если не указано, будет 'Unknown')", example = "Иван Петров")
    @JsonProperty("managerName")
    private String managerName;
    @Schema(description = "Должность менеджера", example = "Старший менеджер")
    @JsonProperty("managerPosition")
    private String managerPosition;
    @Schema(description = "Дата звонка (если нет, подставляется текущая)", example = "2025-04-08")
    @JsonProperty("date")
    private String date;
    @Schema(description = "Длительность в свободном формате", example = "1ч 30м")
    @JsonProperty("duration")
    private String duration;
    @Schema(description = "Длительность в секундах (если не указана, парсится из duration)", example = "5400")
    @JsonProperty("durationSeconds")
    private Integer durationSeconds;
    @Schema(description = "Ссылка на аудиофайл в MinIO (обязательно)", example = "minio://calls/2025/04/08/001.mp3")
    @JsonProperty("fileUrl")
    private String fileUrl;
}




