package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.firefly.core.banking.payments.hub.core.services.InternalTransferService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.InternalTransferProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of the InternalTransferService interface.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class InternalTransferServiceImpl implements InternalTransferService {

    private static final Logger log = LoggerFactory.getLogger(InternalTransferServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public InternalTransferServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized InternalTransferServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateTransfer(InternalTransferRequestDTO request) {
        log.debug("Simulating internal transfer: {}", request);
        return getProvider()
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("Internal transfer simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating internal transfer", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No internal transfer provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeTransfer(InternalTransferRequestDTO request) {
        log.debug("Executing internal transfer: {}", request);
        return getProvider()
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("Internal transfer execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing internal transfer", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No internal transfer provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelTransfer(InternalTransferCancellationRequestDTO request) {
        log.debug("Cancelling internal transfer with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("Internal transfer cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling internal transfer with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No internal transfer provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(InternalTransferCancellationRequestDTO request) {
        log.debug("Simulating cancellation of internal transfer: {}", request);
        return getProvider()
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("Internal transfer cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of internal transfer", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No internal transfer provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleTransfer(InternalTransferRequestDTO request,
                                                         String executionDate,
                                                         String recurrencePattern) {
        log.debug("Scheduling internal transfer: {}, execution date: {}, recurrence: {}",
                request, executionDate, recurrencePattern);
        return getProvider()
                .map(provider -> provider.schedule(request, executionDate, recurrencePattern)
                    .doOnSuccess(result -> log.info("Internal transfer scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling internal transfer", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No internal transfer provider available")));
    }

    /**
     * Gets the internal transfer provider.
     *
     * @return The internal transfer provider
     */
    private Optional<InternalTransferProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.INTERNAL_PROVIDER);
    }
}
