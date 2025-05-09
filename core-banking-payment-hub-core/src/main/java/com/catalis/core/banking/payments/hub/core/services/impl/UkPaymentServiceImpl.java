package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.catalis.core.banking.payments.hub.core.services.UkPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkBacsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkChapsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkFasterPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.providers.UkPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of the UkPaymentService interface.
 * Provides routing logic to delegate operations to the appropriate provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class UkPaymentServiceImpl implements UkPaymentService {

    private static final Logger log = LoggerFactory.getLogger(UkPaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public UkPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized UkPaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateFasterPayment(UkFasterPaymentRequestDTO request) {
        log.debug("Simulating UK Faster Payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateFasterPayment(request)
                    .doOnSuccess(result -> log.info("UK Faster Payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating UK Faster Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeFasterPayment(UkFasterPaymentRequestDTO request) {
        log.debug("Executing UK Faster Payment: {}", request);
        return getProvider()
                .map(provider -> provider.executeFasterPayment(request)
                    .doOnSuccess(result -> log.info("UK Faster Payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing UK Faster Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelFasterPayment(UkCancellationRequestDTO request) {
        log.debug("Cancelling UK Faster Payment with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancelFasterPayment(request)
                    .doOnSuccess(result -> log.info("UK Faster Payment cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling UK Faster Payment with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateFasterPaymentCancellation(UkCancellationRequestDTO request) {
        log.debug("Simulating cancellation of UK Faster Payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateFasterPaymentCancellation(request)
                    .doOnSuccess(result -> log.info("UK Faster Payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of UK Faster Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleFasterPayment(UkFasterPaymentRequestDTO request, String executionDate) {
        log.debug("Scheduling UK Faster Payment: {}, execution date: {}", request, executionDate);
        return getProvider()
                .map(provider -> provider.scheduleFasterPayment(request, executionDate)
                    .doOnSuccess(result -> log.info("UK Faster Payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling UK Faster Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateBacsPayment(UkBacsPaymentRequestDTO request) {
        log.debug("Simulating UK BACS Payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateBacsPayment(request)
                    .doOnSuccess(result -> log.info("UK BACS Payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating UK BACS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeBacsPayment(UkBacsPaymentRequestDTO request) {
        log.debug("Executing UK BACS Payment: {}", request);
        return getProvider()
                .map(provider -> provider.executeBacsPayment(request)
                    .doOnSuccess(result -> log.info("UK BACS Payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing UK BACS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelBacsPayment(UkCancellationRequestDTO request) {
        log.debug("Cancelling UK BACS Payment with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancelBacsPayment(request)
                    .doOnSuccess(result -> log.info("UK BACS Payment cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling UK BACS Payment with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateBacsPaymentCancellation(UkCancellationRequestDTO request) {
        log.debug("Simulating cancellation of UK BACS Payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateBacsPaymentCancellation(request)
                    .doOnSuccess(result -> log.info("UK BACS Payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of UK BACS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleBacsPayment(UkBacsPaymentRequestDTO request, String executionDate) {
        log.debug("Scheduling UK BACS Payment: {}, execution date: {}", request, executionDate);
        return getProvider()
                .map(provider -> provider.scheduleBacsPayment(request, executionDate)
                    .doOnSuccess(result -> log.info("UK BACS Payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling UK BACS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateChapsPayment(UkChapsPaymentRequestDTO request) {
        log.debug("Simulating UK CHAPS Payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateChapsPayment(request)
                    .doOnSuccess(result -> log.info("UK CHAPS Payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating UK CHAPS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeChapsPayment(UkChapsPaymentRequestDTO request) {
        log.debug("Executing UK CHAPS Payment: {}", request);
        return getProvider()
                .map(provider -> provider.executeChapsPayment(request)
                    .doOnSuccess(result -> log.info("UK CHAPS Payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing UK CHAPS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelChapsPayment(UkCancellationRequestDTO request) {
        log.debug("Cancelling UK CHAPS Payment with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancelChapsPayment(request)
                    .doOnSuccess(result -> log.info("UK CHAPS Payment cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling UK CHAPS Payment with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateChapsPaymentCancellation(UkCancellationRequestDTO request) {
        log.debug("Simulating cancellation of UK CHAPS Payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateChapsPaymentCancellation(request)
                    .doOnSuccess(result -> log.info("UK CHAPS Payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of UK CHAPS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleChapsPayment(UkChapsPaymentRequestDTO request, String executionDate) {
        log.debug("Scheduling UK CHAPS Payment: {}, execution date: {}", request, executionDate);
        return getProvider()
                .map(provider -> provider.scheduleChapsPayment(request, executionDate)
                    .doOnSuccess(result -> log.info("UK CHAPS Payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling UK CHAPS Payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No UK payment provider available")));
    }

    /**
     * Gets the appropriate UK payment provider.
     *
     * @return The selected payment provider
     */
    private Optional<UkPaymentProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.UK_PROVIDER);
    }
}
