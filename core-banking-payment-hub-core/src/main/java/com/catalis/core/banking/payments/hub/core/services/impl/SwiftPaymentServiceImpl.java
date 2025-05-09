package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.catalis.core.banking.payments.hub.core.services.SwiftPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftScheduleRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.providers.SwiftPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the SwiftPaymentService interface.
 * Provides routing logic to delegate operations to the appropriate provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class SwiftPaymentServiceImpl implements SwiftPaymentService {

    private static final Logger log = LoggerFactory.getLogger(SwiftPaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public SwiftPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized SwiftPaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(SwiftPaymentRequestDTO request) {
        log.debug("Simulating SWIFT payment: {}", request);
        return getProviderForRequest(request)
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("SWIFT payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating SWIFT payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SWIFT payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(SwiftPaymentRequestDTO request) {
        log.debug("Executing SWIFT payment: {}", request);
        return getProviderForRequest(request)
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("SWIFT payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing SWIFT payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SWIFT payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(SwiftCancellationRequestDTO request) {
        log.debug("Simulating cancellation of SWIFT payment: {}", request);
        return getProviderForCancellation(request)
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("SWIFT payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of SWIFT payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SWIFT payment provider available")));
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(SwiftCancellationRequestDTO request) {
        log.debug("Cancelling SWIFT payment: {}", request);
        return getProviderForCancellation(request)
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("SWIFT payment cancellation completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling SWIFT payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SWIFT payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(SwiftScheduleRequestDTO request) {
        log.debug("Scheduling SWIFT payment: {}", request);
        return getProviderForSchedule(request)
                .map(provider -> provider.schedule(request)
                    .doOnSuccess(result -> log.info("SWIFT payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling SWIFT payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No SWIFT payment provider available")));
    }

    /**
     * Gets the appropriate provider for the payment request.
     *
     * @param request The payment request
     * @return The selected payment provider
     */
    private java.util.Optional<SwiftPaymentProvider> getProviderForRequest(SwiftPaymentRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<SwiftPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
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
    private java.util.Optional<SwiftPaymentProvider> getProviderForCancellation(SwiftCancellationRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<SwiftPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
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
    private java.util.Optional<SwiftPaymentProvider> getProviderForSchedule(SwiftScheduleRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<SwiftPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentRequest().getPaymentType());
    }
}