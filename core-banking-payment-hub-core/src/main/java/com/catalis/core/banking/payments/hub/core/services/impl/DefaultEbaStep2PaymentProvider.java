package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.CancellationUtils;
import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.EbaStep2CancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.EbaStep2PaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentType;
import com.catalis.core.banking.payments.hub.interfaces.providers.EbaStep2PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the EbaStep2PaymentProvider interface.
 * Provides simulation functionality for EBA STEP2 payments.
 */
@Service
public class DefaultEbaStep2PaymentProvider implements EbaStep2PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultEbaStep2PaymentProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(EbaStep2PaymentRequestDTO request) {
        log.info("Simulating EBA STEP2 payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.EBA_STEP2);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.EBA_STEP2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now());
        result.setEstimatedSettlementDate(LocalDate.now().plusDays(1));
        result.setEstimatedFee(new BigDecimal("0.50"));
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
                    request.getSca().getRecipient() : maskPhoneNumber(getDefaultPhoneNumber(request));

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
                ScaResultDTO validationResult = validateSca(request.getSca());
                result.setScaResult(validationResult);
                result.setScaCompleted(validationResult.isSuccess());
            }

            log.info("SCA delivery triggered for EBA STEP2 payment simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(EbaStep2PaymentRequestDTO request) {
        log.info("Executing EBA STEP2 payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.EBA_STEP2);
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.EBA_STEP2_PROVIDER);
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

            ScaResultDTO scaResult = validateSca(request.getSca());
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
        result.setExpectedSettlementDate(LocalDate.now().plusDays(1));
        result.setTransactionReference("STEP2-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("STEP2-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getEndToEndId());

        return Mono.just(result);
    }

    @Override
    @Deprecated
    public Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason) {
        log.info("Cancelling EBA STEP2 payment (deprecated method): {}, reason: {}", paymentId, reason);

        // Create a cancellation request and delegate to the new method
        EbaStep2CancellationRequestDTO request = new EbaStep2CancellationRequestDTO();
        request.setPaymentId(paymentId);
        request.setCancellationReason(reason);
        request.setPaymentType(PaymentType.EBA_STEP2);

        return cancel(request);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(EbaStep2CancellationRequestDTO request) {
        log.info("Cancelling EBA STEP2 payment: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.EBA_STEP2_PROVIDER,
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

        // Check if the payment is still cancellable (in a real implementation, this would check the payment status)
        boolean isCancellable = true;

        if (isCancellable) {
            result.setSuccess(true);
            result.setStatus(PaymentStatus.CANCELLED);
            result.setCancellationDate(LocalDate.now());
        } else {
            result.setSuccess(false);
            result.setStatus(PaymentStatus.REJECTED);
            result.setErrorCode("CANCELLATION_NOT_POSSIBLE");
            result.setErrorMessage("The payment cannot be cancelled at this stage");
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(EbaStep2PaymentRequestDTO request, String executionDate) {
        log.info("Scheduling EBA STEP2 payment: {}, execution date: {}", request, executionDate);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.EBA_STEP2);
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.EBA_STEP2_PROVIDER);
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

            ScaResultDTO scaResult = validateSca(request.getSca());
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
        result.setExpectedSettlementDate(LocalDate.parse(executionDate).plusDays(1));
        result.setTransactionReference("STEP2-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    /**
     * Generates a unique payment ID.
     *
     * @return A unique payment ID
     */
    private String generatePaymentId() {
        return "STEP2-" + UUID.randomUUID().toString();
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for the payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(EbaStep2PaymentRequestDTO request) {
        // In a real implementation, this would check various factors like payment amount, risk, etc.
        // For this simulation, we'll require SCA for all payments
        return true;
    }

    /**
     * Validates the Strong Customer Authentication (SCA) data.
     *
     * @param sca The SCA data to validate
     * @return The result of the SCA validation
     */
    private ScaResultDTO validateSca(ScaDTO sca) {
        // In a real implementation, this would validate the SCA against a backend system
        // For simulation, we'll accept a specific code or generate random success/failure

        ScaResultDTO result = new ScaResultDTO();
        result.setMethod(sca.getMethod());
        result.setChallengeId(sca.getChallengeId() != null ? sca.getChallengeId() : "CHL-" + UUID.randomUUID().toString().substring(0, 8));
        result.setVerificationTimestamp(LocalDateTime.now());
        result.setAttemptCount(1);
        result.setMaxAttempts(3);
        result.setExpired(false);
        result.setExpiryTimestamp(LocalDateTime.now().plusMinutes(15));

        // For testing, accept "123456" as a valid code
        if (sca.getAuthenticationCode() != null && "123456".equals(sca.getAuthenticationCode())) {
            result.setSuccess(true);
            result.setMessage("SCA validation successful");
        } else if (sca.getAuthenticationCode() == null) {
            result.setSuccess(false);
            result.setErrorCode("SCA_CODE_MISSING");
            result.setErrorMessage("Authentication code is required");
        } else {
            // Random success/failure for other codes
            boolean success = Math.random() > 0.3; // 70% success rate
            result.setSuccess(success);
            if (success) {
                result.setMessage("SCA validation successful");
            } else {
                result.setErrorCode("SCA_INVALID_CODE");
                result.setErrorMessage("Invalid authentication code");
            }
        }

        return result;
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
     * Gets a default phone number for the customer based on the request.
     * In a real implementation, this would look up the customer's phone number from a database.
     *
     * @param request The payment request
     * @return A default phone number
     */
    private String getDefaultPhoneNumber(Object request) {
        // In a real implementation, this would look up the customer's phone number
        // For simulation, we'll return a dummy phone number
        return "+33123456789";
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

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(EbaStep2CancellationRequestDTO request) {
        log.info("Simulating EBA STEP2 payment cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.EBA_STEP2_PROVIDER,
                "EUR",
                scaRequired,
                true); // EBA STEP2 payments are generally cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
    }
}
