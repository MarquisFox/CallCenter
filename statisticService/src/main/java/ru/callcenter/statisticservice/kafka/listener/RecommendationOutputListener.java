package ru.vinpin.statisticservice.kafka.listener;

import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.vinpin.statisticservice.dto.kafka.GigachatOutputMessage;
import ru.vinpin.statisticservice.services.CallService;

@Component
@AllArgsConstructor
public class RecommendationOutputListener {
    private final CallService callService;

    @KafkaListener(topics = "${kafka.topics.recommendation-output}", containerFactory = "gigachatOutputListenerContainerFactory")
    public void handleRecommendationOutput(GigachatOutputMessage dto, Acknowledgment ack) {
        callService.updateGigachatResult(dto);
        ack.acknowledge();
    }
}