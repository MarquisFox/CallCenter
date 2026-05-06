package ru.vinpin.statisticservice.dto.kafka;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GigachatOutputMessage {
    private String callId;
    private BigDecimal rating;
    private BigDecimal errorRate;
    private List<ChecklistItemDto> items;

    @Data
    public static class ChecklistItemDto {
        private String code;
        private Boolean completed;
        private Integer penalty;
        private String recommendation;
    }
}