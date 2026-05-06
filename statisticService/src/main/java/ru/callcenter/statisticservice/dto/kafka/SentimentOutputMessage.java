package ru.vinpin.statisticservice.dto.kafka;

import lombok.Data;

@Data
public class SentimentOutputMessage {
    private String callId;
    private String tonality;
}