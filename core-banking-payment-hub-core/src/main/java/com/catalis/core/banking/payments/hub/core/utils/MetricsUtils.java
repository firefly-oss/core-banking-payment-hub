package com.catalis.core.banking.payments.hub.core.utils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for metrics collection and performance monitoring.
 * This class provides methods for timing operations and recording metrics using Micrometer.
 */
@Slf4j
@Component
public class MetricsUtils {

    private static MeterRegistry meterRegistry;

    @Autowired(required = false)
    public MetricsUtils(MeterRegistry meterRegistry) {
        MetricsUtils.meterRegistry = meterRegistry;
    }

    /**
     * Times the execution of a supplier function and logs the duration.
     *
     * @param operationName The name of the operation for logging
     * @param supplier The supplier function to execute
     * @param <T> The return type of the supplier
     * @return The result of the supplier
     */
    public static <T> T timeOperation(String operationName, Supplier<T> supplier) {
        Instant start = Instant.now();
        try {
            T result = supplier.get();
            Duration duration = Duration.between(start, Instant.now());
            log.debug("{} completed in {}ms", operationName, duration.toMillis());
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            log.error("{} failed after {}ms: {}", operationName, duration.toMillis(), e.getMessage());
            throw e;
        }
    }

    /**
     * Records metrics for a payment operation using Micrometer.
     *
     * @param providerName The name of the provider
     * @param operation The operation name
     * @param durationMs The duration of the operation in milliseconds
     * @param success Whether the operation was successful
     */
    public static void recordPaymentMetrics(String providerName, String operation, long durationMs, boolean success) {
        if (meterRegistry == null) {
            log.warn("MeterRegistry not initialized. Metrics will not be recorded.");
            log.debug("METRIC: provider={}, operation={}, duration={}ms, success={}",
                    providerName, operation, durationMs, success);
            return;
        }

        // Record operation duration
        Timer timer = meterRegistry.timer("payment.provider." + providerName + "." + operation);
        timer.record(durationMs, TimeUnit.MILLISECONDS);

        // Record success/failure count
        Counter counter = meterRegistry.counter("payment.provider." + providerName + "." + operation +
            (success ? ".success" : ".failure"));
        counter.increment();

        // Log for debugging
        log.debug("METRIC: provider={}, operation={}, duration={}ms, success={}",
                providerName, operation, durationMs, success);
    }

    /**
     * Records metrics for an SCA operation using Micrometer.
     *
     * @param operation The SCA operation name (trigger, validate)
     * @param method The SCA method (SMS, EMAIL, APP, BIOMETRIC)
     * @param durationMs The duration of the operation in milliseconds
     * @param success Whether the operation was successful
     */
    public static void recordScaMetrics(String operation, String method, long durationMs, boolean success) {
        if (meterRegistry == null) {
            log.warn("MeterRegistry not initialized. Metrics will not be recorded.");
            log.debug("METRIC: sca_operation={}, method={}, duration={}ms, success={}",
                    operation, method, durationMs, success);
            return;
        }

        // Normalize method name for metrics
        String normalizedMethod = method != null ? method.toLowerCase().replace('-', '_') : "unknown";

        // Record operation duration
        Timer timer = meterRegistry.timer("sca." + operation + "." + normalizedMethod);
        timer.record(durationMs, TimeUnit.MILLISECONDS);

        // Record success/failure count
        Counter counter = meterRegistry.counter("sca." + operation + "." + normalizedMethod +
            (success ? ".success" : ".failure"));
        counter.increment();

        // For biometric methods, record additional metrics
        if (normalizedMethod.startsWith("biometric")) {
            Counter biometricCounter = meterRegistry.counter("sca.biometric." + operation +
                (success ? ".success" : ".failure"));
            biometricCounter.increment();
        }

        // Log for debugging
        log.debug("METRIC: sca_operation={}, method={}, duration={}ms, success={}",
                operation, method, durationMs, success);
    }
}
