package ru.vinpin.bitrixadapterservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${statistics.service.url}")
    private String statisticsServiceUrl;

    @Bean
    public WebClient statisticsWebClient() {
        return WebClient.builder()
                .baseUrl(statisticsServiceUrl)
                .build();
    }
}