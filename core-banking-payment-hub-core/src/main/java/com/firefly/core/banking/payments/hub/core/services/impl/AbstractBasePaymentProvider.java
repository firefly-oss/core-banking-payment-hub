package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.core.utils.ScaUtils;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.providers.BasePaymentProvider;
import com.firefly.core.banking.payments.hub.interfaces.providers.ScaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Abstract base implementation of the BasePaymentProvider interface.
 * Provides common functionality for all payment providers.
 */
@Slf4j
public abstract class AbstractBasePaymentProvider implements BasePaymentProvider {

    protected final ScaProvider scaProvider;

    @Autowired
    public AbstractBasePaymentProvider(ScaProvider scaProvider) {
        this.scaProvider = scaProvider;
    }

    @Override
    public Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId) {
        log.info("Triggering SCA for payment: recipient={}, method={}, reference={}",
                ScaUtils.maskPhoneNumber(recipientIdentifier), method, referenceId);

        Instant start = Instant.now();

        // Delegate to the SCA provider
        return scaProvider.triggerSca(recipientIdentifier, method, referenceId)
                .doOnSuccess(result -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("SCA triggered successfully: challengeId={}, method={}, duration={}ms",
                            result.getChallengeId(), result.getMethod(), duration.toMillis());
                    recordMetrics("sca.trigger", duration.toMillis(), true);
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("Error triggering SCA: method={}, error={}, duration={}ms",
                            method, error.getMessage(), duration.toMillis());
                    recordMetrics("sca.trigger", duration.toMillis(), false);
                });
    }

    @Override
    public Mono<ScaResultDTO> validateSca(ScaDTO sca) {
        log.info("Validating SCA for payment: challengeId={}", sca.getChallengeId());

        Instant start = Instant.now();

        // Delegate to the SCA provider
        return scaProvider.validateSca(sca)
                .doOnSuccess(result -> {
                    Duration duration = Duration.between(start, Instant.now());
                    if (result.isSuccess()) {
                        log.info("SCA validation successful: challengeId={}, method={}, duration={}ms",
                                result.getChallengeId(), result.getMethod(), duration.toMillis());
                    } else {
                        log.warn("SCA validation failed: challengeId={}, method={}, errorCode={}, duration={}ms",
                                result.getChallengeId(), result.getMethod(), result.getErrorCode(), duration.toMillis());
                    }
                    recordMetrics("sca.validate", duration.toMillis(), result.isSuccess());
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("Error validating SCA: challengeId={}, error={}, duration={}ms",
                            sca.getChallengeId(), error.getMessage(), duration.toMillis());
                    recordMetrics("sca.validate", duration.toMillis(), false);
                });
    }

    @Override
    public Mono<Boolean> isHealthy() {
        log.debug("Performing health check for {}", getProviderName());
        
        Instant start = Instant.now();
        
        // Default implementation checks if the provider can connect to its dependencies
        return checkProviderHealth()
                .doOnSuccess(healthy -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("Health check for {} completed: healthy={}, duration={}ms",
                            getProviderName(), healthy, duration.toMillis());
                    recordMetrics("health.check", duration.toMillis(), healthy);
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("Error during health check for {}: error={}, duration={}ms",
                            getProviderName(), error.getMessage(), duration.toMillis());
                    recordMetrics("health.check", duration.toMillis(), false);
                })
                .onErrorReturn(false);
    }
    
    /**
     * Checks if the provider is healthy by verifying connectivity to its dependencies.
     * This method should be overridden by subclasses to implement provider-specific health checks.
     *
     * @return A Mono emitting a boolean indicating if the provider is healthy
     */
    protected Mono<Boolean> checkProviderHealth() {
        // Default implementation assumes the provider is healthy
        // Subclasses should override this method to implement provider-specific health checks
        return Mono.just(true);
    }
    
    /**
     * Gets the name of the provider for logging and metrics.
     *
     * @return The provider name
     */
    protected abstract String getProviderName();
    
    /**
     * Records metrics for the provider operation.
     * This is a placeholder method that should be replaced with actual metrics collection
     * in a production environment.
     *
     * @param operation The operation name
     * @param durationMs The duration of the operation in milliseconds
     * @param success Whether the operation was successful
     */
    protected void recordMetrics(String operation, long durationMs, boolean success) {
        // This is a placeholder for actual metrics collection
        // In a real implementation, this would use a metrics library like Micrometer
        // to record metrics for monitoring
        
        // Example:
        // Timer timer = meterRegistry.timer("payment.provider." + getProviderName() + "." + operation);
        // timer.record(durationMs, TimeUnit.MILLISECONDS);
        // 
        // Counter counter = meterRegistry.counter("payment.provider." + getProviderName() + "." + operation + 
        //     (success ? ".success" : ".failure"));
        // counter.increment();
    }
}
