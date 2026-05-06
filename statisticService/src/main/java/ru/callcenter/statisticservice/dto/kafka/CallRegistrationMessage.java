package ru.vinpin.statisticservice.dto.kafka;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CallRegistrationMessage {
    private String callId;
    private String managerId;
    private String managerName;
    private String managerPosition;
    private LocalDate date;
    private String duration;
    private Integer durationSeconds;
    private String fileUrl;
}