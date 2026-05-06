package ru.vinpin.statisticservice.dto.api;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ManagerErrorDto {
    private Long id;
    private String name;
    private BigDecimal errorRate;
}
