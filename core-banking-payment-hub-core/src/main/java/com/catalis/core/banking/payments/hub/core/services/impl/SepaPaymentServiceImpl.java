package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.catalis.core.banking.payments.hub.core.services.SepaPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.providers.SepaPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the SepaPaymentService interface.
 * Provides routing logic to delegate operations to the appropriate provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class SepaPaymentServiceImpl implements SepaPaymentService {

    private static final Logger log = LoggerFactory.getLogger(SepaPaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public SepaPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized SepaPaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(SepaPaymentRequestDTO request) {
        log.debug("Simulating SEPA payment: {}", request);
        return getProviderForRequest(request)
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("SEPA payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating SEPA payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SEPA payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(SepaPaymentRequestDTO request) {
        log.debug("Executing SEPA payment: {}", request);
        return getProviderForRequest(request)
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("SEPA payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing SEPA payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SEPA payment provider available")));
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(SepaCancellationRequestDTO request) {
        log.debug("Cancelling SEPA payment: {}", request);
        return getProviderForCancellation(request)
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("SEPA payment cancellation completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling SEPA payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SEPA payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(SepaScheduleRequestDTO request) {
        log.debug("Scheduling SEPA payment: {}", request);
        return getProviderForSchedule(request)
                .map(provider -> provider.schedule(request)
                    .doOnSuccess(result -> log.info("SEPA payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling SEPA payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SEPA payment provider available")));
    }

    /**
     * Gets the appropriate provider for the payment request.
     *
     * @param request The payment request
     * @return The selected payment provider
     */
    private java.util.Optional<SepaPaymentProvider> getProviderForRequest(SepaPaymentRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<SepaPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentType());
    }

    /**
     * Gets the appropriate provider for the cancellation request.
     *
     * @param request The cancellation request
     * @return The selected payment provider
     */
    private java.util.Optional<SepaPaymentProvider> getProviderForCancellation(SepaCancellationRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<SepaPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentType());
    }

    /**
     * Gets the appropriate provider for the schedule request.
     *
     * @param request The schedule request
     * @return The selected payment provider
     */
    private java.util.Optional<SepaPaymentProvider> getProviderForSchedule(SepaScheduleRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPaymentRequest().getPreferredProvider() != null) {
            java.util.Optional<SepaPaymentProvider> provider = providerRegistry.getProvider(request.getPaymentRequest().getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentRequest().getPaymentType());
    }
}