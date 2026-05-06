package ru.vinpin.statisticservice.dto.api;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CallDto {
    private String id;
    private LocalDate date;
    private String manager;
    private String duration;
    private String tonality;
}
