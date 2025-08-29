package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.firefly.core.banking.payments.hub.core.services.EbaStep2PaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.EbaStep2CancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.EbaStep2PaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.EbaStep2PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of the EbaStep2PaymentService interface.
 * Provides routing logic to delegate operations to the appropriate provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class EbaStep2PaymentServiceImpl implements EbaStep2PaymentService {

    private static final Logger log = LoggerFactory.getLogger(EbaStep2PaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public EbaStep2PaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized EbaStep2PaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(EbaStep2PaymentRequestDTO request) {
        log.debug("Simulating EBA STEP2 payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("EBA STEP2 payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating EBA STEP2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No EBA STEP2 payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(EbaStep2PaymentRequestDTO request) {
        log.debug("Executing EBA STEP2 payment: {}", request);
        return getProvider()
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("EBA STEP2 payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing EBA STEP2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No EBA STEP2 payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(EbaStep2CancellationRequestDTO request) {
        log.debug("Cancelling EBA STEP2 payment with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("EBA STEP2 payment cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling EBA STEP2 payment with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No EBA STEP2 payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(EbaStep2CancellationRequestDTO request) {
        log.debug("Simulating cancellation of EBA STEP2 payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("EBA STEP2 payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of EBA STEP2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No EBA STEP2 payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(EbaStep2PaymentRequestDTO request, String executionDate) {
        log.debug("Scheduling EBA STEP2 payment: {}, execution date: {}", request, executionDate);
        return getProvider()
                .map(provider -> provider.schedule(request, executionDate)
                    .doOnSuccess(result -> log.info("EBA STEP2 payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling EBA STEP2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No EBA STEP2 payment provider available")));
    }

    /**
     * Gets the appropriate EBA STEP2 payment provider.
     *
     * @return The selected payment provider
     */
    private Optional<EbaStep2PaymentProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.EBA_STEP2_PROVIDER);
    }
}
