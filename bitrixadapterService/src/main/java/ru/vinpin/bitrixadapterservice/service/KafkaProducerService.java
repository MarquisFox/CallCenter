package ru.vinpin.bitrixadapterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.call-registration}")
    private String topic;

    public void send(String key, String jsonMessage) {
        kafkaTemplate.send(topic, key, jsonMessage)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Message sent to {} with key {}, offset {}", topic, key, result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message to {}", topic, ex);
                        throw new RuntimeException("Kafka send failed", ex);
                    }
                });
    }
}