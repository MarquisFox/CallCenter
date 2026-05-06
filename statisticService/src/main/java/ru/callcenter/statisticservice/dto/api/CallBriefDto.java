package ru.vinpin.statisticservice.dto.api;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CallBriefDto {
    private String id;
    private LocalDate date;
    private String duration;
    private String tonality;
}