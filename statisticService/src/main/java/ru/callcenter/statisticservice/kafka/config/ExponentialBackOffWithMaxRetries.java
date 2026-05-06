package ru.vinpin.statisticservice.kafka.config;

import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

public class ExponentialBackOffWithMaxRetries implements BackOff {
    private final ExponentialBackOff backOff;
    private final int maxAttempts;

    public ExponentialBackOffWithMaxRetries(long initialInterval, double multiplier, long maxInterval, int maxAttempts) {
        this.backOff = new ExponentialBackOff(initialInterval, multiplier);
        this.backOff.setMaxInterval(maxInterval);
        this.maxAttempts = maxAttempts;
    }

    @Override
    public BackOffExecution start() {
        return new BackOffExecution() {
            private int attempt = 0;
            private final BackOffExecution delegate = backOff.start();

            @Override
            public long nextBackOff() {
                if (attempt++ >= maxAttempts) return STOP;
                return delegate.nextBackOff();
            }
        };
    }
}