package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.firefly.core.banking.payments.hub.core.services.CardPaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardScheduleRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.CardPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the CardPaymentService interface.
 * Acts as the card authorization center service layer, providing routing logic 
 * to delegate operations to the appropriate card payment provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class CardPaymentServiceImpl implements CardPaymentService {

    private static final Logger log = LoggerFactory.getLogger(CardPaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public CardPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized CardPaymentServiceImpl with provider registry - Card authorization center ready");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(CardPaymentRequestDTO request) {
        log.debug("Simulating card payment: {}", request);
        return getProviderForRequest(request)
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("Card payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating card payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No card payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(CardPaymentRequestDTO request) {
        log.debug("Executing card payment authorization: {}", request);
        return getProviderForRequest(request)
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("Card payment authorization completed: {}", result))
                    .doOnError(error -> log.error("Error authorizing card payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No card payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(CardCancellationRequestDTO request) {
        log.debug("Simulating cancellation of card payment: {}", request);
        return getProviderForCancellation(request)
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("Card payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of card payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No card payment provider available")));
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(CardCancellationRequestDTO request) {
        log.debug("Cancelling card payment: {}", request);
        return getProviderForCancellation(request)
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("Card payment cancellation completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling card payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No card payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(CardScheduleRequestDTO request) {
        log.debug("Scheduling card payment: {}", request);
        return getProviderForSchedule(request)
                .map(provider -> provider.schedule(request)
                    .doOnSuccess(result -> log.info("Card payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling card payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No card payment provider available")));
    }

    /**
     * Gets the appropriate provider for the card payment request.
     *
     * @param request The card payment request
     * @return The selected card payment provider
     */
    private java.util.Optional<CardPaymentProvider> getProviderForRequest(CardPaymentRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<CardPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentType());
    }

    /**
     * Gets the appropriate provider for the card cancellation request.
     *
     * @param request The card cancellation request
     * @return The selected card payment provider
     */
    private java.util.Optional<CardPaymentProvider> getProviderForCancellation(CardCancellationRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPreferredProvider() != null) {
            java.util.Optional<CardPaymentProvider> provider = providerRegistry.getProvider(request.getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentType());
    }

    /**
     * Gets the appropriate provider for the card schedule request.
     *
     * @param request The card schedule request
     * @return The selected card payment provider
     */
    private java.util.Optional<CardPaymentProvider> getProviderForSchedule(CardScheduleRequestDTO request) {
        // First try to get provider based on preferred provider in the request
        if (request.getPaymentRequest().getPreferredProvider() != null) {
            java.util.Optional<CardPaymentProvider> provider = providerRegistry.getProvider(request.getPaymentRequest().getPreferredProvider());
            if (provider.isPresent()) {
                return provider;
            }
        }

        // Then try to get provider based on payment type
        return providerRegistry.getProviderForPaymentType(request.getPaymentRequest().getPaymentType());
    }
}