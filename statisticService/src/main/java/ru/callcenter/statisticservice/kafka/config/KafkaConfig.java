package ru.vinpin.statisticservice.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import ru.vinpin.statisticservice.dto.kafka.CallRegistrationMessage;
import ru.vinpin.statisticservice.dto.kafka.GigachatOutputMessage;
import ru.vinpin.statisticservice.dto.kafka.SentimentOutputMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> dlqProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                            @Value("${retry.max-attempts:5}") int maxAttempts,
                                            @Value("${retry.backoff-ms:1000}") long backoffMs,
                                            @Value("${retry.backoff-multiplier:2.0}") double multiplier,
                                            @Value("${retry.max-delay-ms:60000}") long maxDelayMs) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + "-dlq", record.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(backoffMs, multiplier, maxDelayMs, maxAttempts);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, CallRegistrationMessage> callRegistrationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics_service_group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<CallRegistrationMessage> deserializer = new JsonDeserializer<>(CallRegistrationMessage.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CallRegistrationMessage> callRegistrationListenerContainerFactory(
            DefaultErrorHandler errorHandler,
            DefaultKafkaConsumerFactory<String, CallRegistrationMessage> callRegistrationConsumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, CallRegistrationMessage>();
        factory.setConsumerFactory(callRegistrationConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, SentimentOutputMessage> sentimentOutputConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics_service_group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<SentimentOutputMessage> deserializer = new JsonDeserializer<>(SentimentOutputMessage.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SentimentOutputMessage> sentimentOutputListenerContainerFactory(
            DefaultErrorHandler errorHandler,
            DefaultKafkaConsumerFactory<String, SentimentOutputMessage> sentimentOutputConsumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SentimentOutputMessage>();
        factory.setConsumerFactory(sentimentOutputConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, GigachatOutputMessage> gigachatOutputConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics_service_group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<GigachatOutputMessage> deserializer = new JsonDeserializer<>(GigachatOutputMessage.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GigachatOutputMessage> gigachatOutputListenerContainerFactory(
            DefaultErrorHandler errorHandler,
            DefaultKafkaConsumerFactory<String, GigachatOutputMessage> gigachatOutputConsumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, GigachatOutputMessage>();
        factory.setConsumerFactory(gigachatOutputConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
