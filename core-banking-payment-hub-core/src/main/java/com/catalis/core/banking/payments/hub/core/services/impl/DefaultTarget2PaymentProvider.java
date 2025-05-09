package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.CancellationUtils;
import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.Target2CancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.Target2PaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentType;
import com.catalis.core.banking.payments.hub.interfaces.providers.Target2PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the Target2PaymentProvider interface.
 * Provides simulation functionality for TARGET2 payments.
 */
@Service
public class DefaultTarget2PaymentProvider implements Target2PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultTarget2PaymentProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(Target2PaymentRequestDTO request) {
        log.info("Simulating TARGET2 payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.TARGET2);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.TARGET2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now());
        result.setEstimatedSettlementDate(LocalDate.now());
        result.setEstimatedFee(new BigDecimal("2.50"));
        result.setFeeCurrency(request.getCurrency());
        result.setEstimatedExchangeRate(null); // No exchange rate for euro payments
        result.setFeasible(true);

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed
        result.setSimulationReference("SIM-" + UUID.randomUUID().toString().substring(0, 8));

        if (scaRequired) {
            // Always trigger SCA delivery during simulation if required
            result.setScaDeliveryTriggered(true);
            result.setScaDeliveryTimestamp(LocalDateTime.now());

            // Determine SCA method and recipient
            String scaMethod = request.getSca() != null && request.getSca().getMethod() != null ?
                    request.getSca().getMethod() : "SMS"; // Default to SMS if not specified
            String scaRecipient = request.getSca() != null && request.getSca().getRecipient() != null ?
                    request.getSca().getRecipient() : ScaUtils.maskPhoneNumber(getDefaultPhoneNumber(request));

            result.setScaDeliveryMethod(scaMethod);
            result.setScaDeliveryRecipient(scaRecipient);
            result.setScaExpiryTimestamp(LocalDateTime.now().plusMinutes(15)); // SCA code valid for 15 minutes

            // Create SCA result with challenge information
            ScaResultDTO scaResult = new ScaResultDTO();
            scaResult.setMethod(scaMethod);
            scaResult.setChallengeId("CHL-" + UUID.randomUUID().toString().substring(0, 8));
            scaResult.setVerificationTimestamp(null); // Not verified yet
            scaResult.setAttemptCount(0);
            scaResult.setMaxAttempts(3);
            scaResult.setExpired(false);
            scaResult.setExpiryTimestamp(result.getScaExpiryTimestamp());
            scaResult.setSuccess(false); // Not verified yet

            result.setScaResult(scaResult);

            // If SCA code is already provided in the request, validate it
            if (request.getSca() != null && request.getSca().getAuthenticationCode() != null) {
                ScaResultDTO validationResult = ScaUtils.validateSca(request.getSca());
                result.setScaResult(validationResult);
                result.setScaCompleted(validationResult.isSuccess());
            }

            log.info("SCA delivery triggered for TARGET2 payment simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(Target2PaymentRequestDTO request) {
        log.info("Executing TARGET2 payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.TARGET2);
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.TARGET2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired) {
            // Check if a simulation reference is provided
            String simulationReference = request.getSimulationReference();
            if (simulationReference != null && !simulationReference.isEmpty()) {
                log.info("Using simulation reference for SCA validation: {}", simulationReference);
                // In a real implementation, we would look up the simulation details using the reference
                // and validate the SCA code against the previously delivered code
            }

            if (request.getSca() == null) {
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required for this payment");
                return Mono.just(result);
            }

            ScaResultDTO scaResult = ScaUtils.validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getMessage());
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now());
        result.setTransactionReference("T2-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("T2-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getEndToEndId());

        return Mono.just(result);
    }

    @Override
    @Deprecated
    public Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason) {
        log.info("Cancelling TARGET2 payment (deprecated method): {}, reason: {}", paymentId, reason);

        // Create a cancellation request and delegate to the new method
        Target2CancellationRequestDTO request = new Target2CancellationRequestDTO();
        request.setPaymentId(paymentId);
        request.setCancellationReason(reason);
        request.setPaymentType(PaymentType.TARGET2);

        return cancel(request);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(Target2CancellationRequestDTO request) {
        log.info("Cancelling TARGET2 payment: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.TARGET2_PROVIDER,
                request.getCancellationReason(),
                scaRequired);

        // Check if a simulation reference is provided
        String simulationReference = request.getSimulationReference();
        if (simulationReference != null && !simulationReference.isEmpty()) {
            log.info("Using simulation reference for SCA validation in cancellation: {}", simulationReference);
            // In a real implementation, we would look up the simulation details using the reference
            // and validate the SCA code against the previously delivered code
        }

        // Validate SCA if required
        if (scaRequired) {
            boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
            if (!scaValid) {
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        // However, TARGET2 payments are generally not cancellable once submitted
        CancellationUtils.setCancellationNotSupported(result, "TARGET2");

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(Target2CancellationRequestDTO request) {
        log.info("Simulating TARGET2 payment cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.TARGET2_PROVIDER,
                "EUR",
                scaRequired,
                false); // TARGET2 payments are generally not cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(Target2PaymentRequestDTO request, String executionDate) {
        log.info("Scheduling TARGET2 payment: {}, execution date: {}", request, executionDate);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.TARGET2);
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.TARGET2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired) {
            if (request.getSca() == null) {
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required for this payment");
                return Mono.just(result);
            }

            ScaResultDTO scaResult = ScaUtils.validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getMessage());
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.SCHEDULED);
        result.setScheduledExecutionDate(LocalDate.parse(executionDate));
        result.setExpectedSettlementDate(LocalDate.parse(executionDate));
        result.setTransactionReference("T2-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    /**
     * Generates a unique payment ID.
     *
     * @return A unique payment ID
     */
    private String generatePaymentId() {
        return "T2-" + UUID.randomUUID().toString();
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for the payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(Target2PaymentRequestDTO request) {
        // In a real implementation, this would check various factors like payment amount, risk, etc.
        // For this simulation, we'll require SCA for all payments
        return true;
    }

    /**
     * Determines if a payment is considered high-value based on its ID.
     * In a real implementation, this would look up the payment details.
     *
     * @param paymentId The payment ID
     * @return true if it's a high-value payment requiring SCA for cancellation
     */
    private boolean isHighValuePayment(String paymentId) {
        // For simulation purposes, we'll require SCA for 50% of cancellations randomly
        return Math.random() > 0.5;
    }

    /**
     * Gets a default phone number for the customer based on the request.
     * In a real implementation, this would look up the customer's phone number from a database.
     *
     * @param request The payment request
     * @return A default phone number
     */
    private String getDefaultPhoneNumber(Object request) {
        // In a real implementation, this would look up the customer's phone number
        // For simulation, we'll return a dummy phone number
        return "+49123456789";
    }
}
