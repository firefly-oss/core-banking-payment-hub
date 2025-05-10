package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkBacsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkChapsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkFasterPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.providers.ScaProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.UkPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Default implementation of the UkPaymentProvider interface.
 * Handles UK payments for different payment types (FPS, BACS, CHAPS).
 */
@Component
public class DefaultUkPaymentProvider implements UkPaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultUkPaymentProvider.class);
    
    private final ScaProvider scaProvider;
    
    @Autowired
    public DefaultUkPaymentProvider(ScaProvider scaProvider) {
        this.scaProvider = scaProvider;
    }

    @Override
    public Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId) {
        log.info("Triggering SCA for UK payment: recipient={}, method={}, reference={}", 
                maskPhoneNumber(recipientIdentifier), method, referenceId);
        
        // Delegate to the SCA provider
        return scaProvider.triggerSca(recipientIdentifier, method, referenceId);
    }

    @Override
    public Mono<ScaResultDTO> validateSca(ScaDTO sca) {
        log.info("Validating SCA for UK payment: challengeId={}", sca.getChallengeId());
        
        // Delegate to the SCA provider
        return scaProvider.validateSca(sca);
    }
    
    @Override
    public Mono<Boolean> isHealthy() {
        // Perform health check for UK payment provider
        log.debug("Performing health check for UK payment provider");
        
        // For demonstration, we'll return a healthy status
        return Mono.just(true);
    }
    
    /**
     * Masks a phone number for privacy, showing only the last 4 digits.
     *
     * @param phoneNumber The phone number to mask
     * @return The masked phone number
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return phoneNumber;
        }
        return "*****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
    
    /**
     * Validates the provided SCA information synchronously.
     *
     * @param sca The SCA information to validate
     * @return The validation result
     */
    private ScaResultDTO validateScaSync(ScaDTO sca) {
        return scaProvider.validateSca(sca).block();
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateFasterPayment(UkFasterPaymentRequestDTO request) {
        log.info("Simulating UK Faster Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeFasterPayment(UkFasterPaymentRequestDTO request) {
        log.info("Executing UK Faster Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancelFasterPayment(UkCancellationRequestDTO request) {
        log.info("Cancelling UK Faster Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateFasterPaymentCancellation(UkCancellationRequestDTO request) {
        log.info("Simulating UK Faster Payment cancellation: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleFasterPayment(UkFasterPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling UK Faster Payment: {}, execution date: {}", request, executionDate);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateBacsPayment(UkBacsPaymentRequestDTO request) {
        log.info("Simulating UK BACS Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeBacsPayment(UkBacsPaymentRequestDTO request) {
        log.info("Executing UK BACS Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancelBacsPayment(UkCancellationRequestDTO request) {
        log.info("Cancelling UK BACS Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateBacsPaymentCancellation(UkCancellationRequestDTO request) {
        log.info("Simulating UK BACS Payment cancellation: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleBacsPayment(UkBacsPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling UK BACS Payment: {}, execution date: {}", request, executionDate);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateChapsPayment(UkChapsPaymentRequestDTO request) {
        log.info("Simulating UK CHAPS Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeChapsPayment(UkChapsPaymentRequestDTO request) {
        log.info("Executing UK CHAPS Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancelChapsPayment(UkCancellationRequestDTO request) {
        log.info("Cancelling UK CHAPS Payment: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateChapsPaymentCancellation(UkCancellationRequestDTO request) {
        log.info("Simulating UK CHAPS Payment cancellation: {}", request);
        // Implementation will be added in the next step
        return Mono.empty();
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleChapsPayment(UkChapsPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling UK CHAPS Payment: {}, execution date: {}", request, executionDate);
        // Implementation will be added in the next step
        return Mono.empty();
    }
}
