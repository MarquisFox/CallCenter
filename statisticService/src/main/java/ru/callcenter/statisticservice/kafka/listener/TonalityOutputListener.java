package ru.vinpin.statisticservice.kafka.listener;

import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.vinpin.statisticservice.dto.kafka.SentimentOutputMessage;
import ru.vinpin.statisticservice.services.CallService;

@Component
@AllArgsConstructor
public class TonalityOutputListener {
    private final CallService callService;

    @KafkaListener(topics = "${kafka.topics.tonality-output}", containerFactory = "sentimentOutputListenerContainerFactory")
    public void handleTonalityOutput(SentimentOutputMessage dto, Acknowledgment ack) {
        callService.updateTonality(dto);
        ack.acknowledge();
    }
}