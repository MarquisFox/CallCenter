package ru.vinpin.statisticservice.dto.api;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ManagerDetailsDto {
    private Long id;
    private String name;
    private BigDecimal rating;
    private BigDecimal errorRate;
    private List<ErrorDescriptionDto> errors;
    private List<CallBriefDto> calls;
}
