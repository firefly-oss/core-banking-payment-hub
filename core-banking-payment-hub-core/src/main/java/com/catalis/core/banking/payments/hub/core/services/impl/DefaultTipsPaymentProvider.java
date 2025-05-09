package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.CancellationUtils;
import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.TipsCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.TipsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentType;
import com.catalis.core.banking.payments.hub.interfaces.providers.TipsPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the TipsPaymentProvider interface.
 * Provides simulation functionality for TIPS (TARGET Instant Payment Settlement) payments.
 */
@Service
public class DefaultTipsPaymentProvider implements TipsPaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultTipsPaymentProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(TipsPaymentRequestDTO request) {
        log.info("Simulating TIPS payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.TIPS);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.TIPS_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now());
        result.setEstimatedSettlementDate(LocalDate.now());
        result.setEstimatedFee(new BigDecimal("0.25"));
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

            log.info("SCA delivery triggered for TIPS payment simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(TipsPaymentRequestDTO request) {
        log.info("Executing TIPS payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.TIPS);
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.TIPS_PROVIDER);
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
        result.setTransactionReference("TIPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("TIPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getEndToEndId());

        // Handle beneficiary notification if required
        if (Boolean.TRUE.equals(request.getBeneficiaryNotificationRequired())) {
            log.info("Beneficiary notification required. Method: {}, Contact: {}",
                    request.getBeneficiaryNotificationMethod(),
                    request.getBeneficiaryNotificationContact());
            // In a real implementation, this would send a notification to the beneficiary
        }

        // Handle originator notification if required
        if (Boolean.TRUE.equals(request.getOriginatorNotificationRequired())) {
            log.info("Originator notification required. Method: {}, Contact: {}",
                    request.getOriginatorNotificationMethod(),
                    request.getOriginatorNotificationContact());
            // In a real implementation, this would send a notification to the originator
        }

        return Mono.just(result);
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancel(TipsCancellationRequestDTO request) {
        log.info("Cancelling TIPS payment: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.TIPS_PROVIDER,
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
        // However, TIPS payments are generally not cancellable once submitted
        CancellationUtils.setCancellationNotSupported(result, "TIPS");

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(TipsCancellationRequestDTO request) {
        log.info("Simulating TIPS payment cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.TIPS_PROVIDER,
                "EUR",
                scaRequired,
                false); // TIPS payments are generally not cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(TipsPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling TIPS payment: {}, execution date: {}", request, executionDate);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.TIPS);
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.TIPS_PROVIDER);
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
        result.setTransactionReference("TIPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    /**
     * Generates a unique payment ID.
     *
     * @return A unique payment ID
     */
    private String generatePaymentId() {
        return "TIPS-" + UUID.randomUUID().toString();
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for the payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(TipsPaymentRequestDTO request) {
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
        return "+39123456789";
    }
}
