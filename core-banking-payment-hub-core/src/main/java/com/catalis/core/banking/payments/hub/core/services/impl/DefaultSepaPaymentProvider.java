package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.MetricsUtils;
import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.providers.ScaProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.SepaPaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the SepaPaymentProvider interface.
 * Extends the AbstractBasePaymentProvider for standardized SCA handling and metrics.
 */
@Slf4j
@Component
public class DefaultSepaPaymentProvider extends AbstractBasePaymentProvider implements SepaPaymentProvider {

    @Autowired
    public DefaultSepaPaymentProvider(ScaProvider scaProvider) {
        super(scaProvider);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(SepaPaymentRequestDTO request) {
        log.info("Simulating SEPA payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now().plusDays(1));
        result.setEstimatedSettlementDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate().plusDays(1) : LocalDate.now().plusDays(2));
        result.setEstimatedFee(new BigDecimal("2.50"));
        result.setFeeCurrency(request.getCurrency());
        result.setFeasible(true);
        result.setSimulationReference("SIM-" + UUID.randomUUID().toString().substring(0, 8));

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

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
                ScaResultDTO validationResult = validateScaSync(request.getSca());
                result.setScaResult(validationResult);
                result.setScaCompleted(validationResult.isSuccess());
            }

            log.info("SCA delivery triggered for SEPA payment simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(SepaPaymentRequestDTO request) {
        log.info("Executing SEPA payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
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
                // SCA is required but not provided
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required for this payment");
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }

            ScaResultDTO scaResult = validateScaSync(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                // SCA validation failed
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getMessage());
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now().plusDays(1));
        result.setTransactionReference("TRN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("CSR-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(SepaCancellationRequestDTO request) {
        log.info("Simulating cancellation of SEPA payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(request.getPaymentId());
        result.setRequestId(UUID.randomUUID().toString());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE_CANCELLATION);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setFeasible(true);
        result.setSimulationReference("SIM-" + UUID.randomUUID().toString().substring(0, 8));

        // For cancellation, we'll require SCA for high-value payments
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired) {
            // Always trigger SCA delivery during simulation if required
            result.setScaDeliveryTriggered(true);
            result.setScaDeliveryTimestamp(LocalDateTime.now());

            // Determine SCA method and recipient
            String scaMethod = request.getSca() != null && request.getSca().getMethod() != null ?
                    request.getSca().getMethod() : "SMS"; // Default to SMS if not specified
            String scaRecipient = request.getSca() != null && request.getSca().getRecipient() != null ?
                    request.getSca().getRecipient() : maskPhoneNumber(getDefaultPhoneNumber(null));

            LocalDateTime expiryTimestamp = LocalDateTime.now().plusMinutes(15); // SCA code valid for 15 minutes
            result.setScaDeliveryMethod(scaMethod);
            result.setScaDeliveryRecipient(scaRecipient);
            result.setScaExpiryTimestamp(expiryTimestamp);

            // Create SCA result with challenge information
            ScaResultDTO scaResult = new ScaResultDTO();
            scaResult.setMethod(scaMethod);
            scaResult.setChallengeId("CHL-" + UUID.randomUUID().toString().substring(0, 8));
            scaResult.setVerificationTimestamp(null); // Not verified yet
            scaResult.setAttemptCount(0);
            scaResult.setMaxAttempts(3);
            scaResult.setExpired(false);
            scaResult.setExpiryTimestamp(expiryTimestamp);
            scaResult.setSuccess(false); // Not verified yet

            result.setScaResult(scaResult);

            // If SCA code is already provided in the request, validate it
            if (request.getSca() != null && request.getSca().getAuthenticationCode() != null) {
                ScaResultDTO validationResult = validateScaSync(request.getSca());
                result.setScaResult(validationResult);
                result.setScaCompleted(validationResult.isSuccess());
            }

            log.info("SCA delivery triggered for SEPA payment cancellation simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(SepaCancellationRequestDTO request) {
        log.info("Cancelling SEPA payment: {}", request);

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(request.getPaymentId());
        result.setRequestId(UUID.randomUUID().toString());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // For cancellation, we'll require SCA for high-value payments
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        // Check if a simulation reference is provided
        String simulationReference = request.getSimulationReference();
        if (simulationReference != null && !simulationReference.isEmpty()) {
            log.info("Using simulation reference for SCA validation in cancellation: {}", simulationReference);
            // In a real implementation, we would look up the simulation details using the reference
            // and validate the SCA code against the previously delivered code
        }

        if (scaRequired) {
            if (request.getSca() == null) {
                // SCA is required but not provided
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required to cancel this payment");
                return Mono.just(result);
            }

            ScaResultDTO scaResult = validateScaSync(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                // SCA validation failed
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getMessage());
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.CANCELLED);
        result.setCancellationReason(request.getCancellationReason());
        result.setCancellationReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setCancellationTimestamp(LocalDateTime.now());
        result.setFeesCharged(false);
        result.setFullyCancelled(true);
        result.setFundsReturned(true);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(SepaScheduleRequestDTO request) {
        log.info("Scheduling SEPA payment: {}", request);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getPaymentRequest().getRequestId());
        result.setPaymentType(request.getPaymentRequest().getPaymentType());
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request.getPaymentRequest());
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        // Check if a simulation reference is provided
        String simulationReference = request.getPaymentRequest().getSimulationReference();
        if (simulationReference != null && !simulationReference.isEmpty()) {
            log.info("Using simulation reference for SCA validation in scheduling: {}", simulationReference);
            // In a real implementation, we would look up the simulation details using the reference
            // and validate the SCA code against the previously delivered code
        }

        if (scaRequired) {
            if (request.getPaymentRequest().getSca() == null) {
                // SCA is required but not provided
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required to schedule this payment");
                return Mono.just(result);
            }

            ScaResultDTO scaResult = validateScaSync(request.getPaymentRequest().getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                // SCA validation failed
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
        result.setScheduledDate(request.getExecutionDate());
        result.setScheduleReference("SCH-" + UUID.randomUUID().toString().substring(0, 8));
        result.setScheduledTimestamp(LocalDateTime.now());
        result.setModifiable(true);
        result.setCancellable(true);
        result.setRecurrencePattern(request.getRecurrencePattern());
        result.setRecurrenceEndDate(request.getRecurrenceEndDate());

        return Mono.just(result);
    }

    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for a payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(SepaPaymentRequestDTO request) {
        // Implement SCA requirement logic based on various factors
        // For example, require SCA for payments above a certain amount
        if (request.getAmount().compareTo(new BigDecimal("30")) > 0) {
            return true;
        }

        // For simulation purposes, we'll require SCA for 50% of payments randomly
        return Math.random() > 0.5;
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
     * Validates the provided SCA information.
     * This is a synchronous wrapper around the reactive validateSca method.
     *
     * @param sca The SCA information to validate
     * @return The validation result
     */
    private ScaResultDTO validateScaSync(ScaDTO sca) {
        // Call the reactive method and block to get the result
        // In a real implementation, we would avoid blocking and use reactive patterns throughout
        return validateSca(sca).block();
    }

    // SCA methods are now inherited from AbstractBasePaymentProvider

    @Override
    protected Mono<Boolean> checkProviderHealth() {
        // Perform health check for SEPA payment provider
        // This could include checking connectivity to external SEPA systems
        log.debug("Checking connectivity to SEPA payment systems");

        // For demonstration, we'll return a healthy status
        // In a real implementation, this would check connectivity to SEPA systems
        return Mono.just(true);
    }

    @Override
    protected String getProviderName() {
        return "sepa";
    }

    /**
     * Gets a default phone number for the customer based on the request.
     * In a real implementation, this would look up the customer's phone number from a database.
     *
     * @param request The payment request
     * @return A default phone number
     */
    private String getDefaultPhoneNumber(SepaPaymentRequestDTO request) {
        // In a real implementation, this would look up the customer's phone number
        // For simulation, we'll return a dummy phone number
        return "+1234567890";
    }
}
