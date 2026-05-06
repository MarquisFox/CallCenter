package ru.vinpin.bitrixadapterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

    private final WebClient statisticsWebClient;

    public Mono<String> proxyGet(String path, Consumer<UriBuilder> queryParams) {
        return statisticsWebClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder.path(path);
                    if (queryParams != null) {
                        queryParams.accept(builder);
                    }
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("StatsService error: " + body))))
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Proxy error for path {}: {}", path, e.getMessage());
                    return Mono.just("{\"error\":\"Statistics service unavailable\"}");
                });
    }
}
