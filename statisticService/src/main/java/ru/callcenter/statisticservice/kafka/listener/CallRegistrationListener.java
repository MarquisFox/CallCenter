package ru.vinpin.statisticservice.kafka.listener;

import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.vinpin.statisticservice.dto.kafka.CallRegistrationMessage;
import ru.vinpin.statisticservice.services.CallService;

@Component
@AllArgsConstructor
public class CallRegistrationListener {
    private final CallService callService;

    @KafkaListener(topics = "${kafka.topics.call-registration}", containerFactory = "callRegistrationListenerContainerFactory")
    public void handleCallRegistration(CallRegistrationMessage dto, Acknowledgment ack) {
        callService.createCall(dto);
        ack.acknowledge();
    }
}