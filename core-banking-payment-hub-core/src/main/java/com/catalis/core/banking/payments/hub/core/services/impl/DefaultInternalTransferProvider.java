package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.CancellationUtils;
import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.providers.InternalTransferProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.ScaProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the InternalTransferProvider interface.
 * Handles transfers between accounts within the core banking system.
 */
@Component
public class DefaultInternalTransferProvider implements InternalTransferProvider {

    private final ScaProvider scaProvider;

    @Autowired
    public DefaultInternalTransferProvider(ScaProvider scaProvider) {
        this.scaProvider = scaProvider;
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultInternalTransferProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(InternalTransferRequestDTO request) {
        log.info("Simulating internal transfer: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.INTERNAL_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now());
        result.setEstimatedSettlementDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now());
        result.setEstimatedFee(null); // No fees for internal transfers
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

            log.info("SCA delivery triggered for internal transfer simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(InternalTransferRequestDTO request) {
        log.info("Executing internal transfer: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.INTERNAL_PROVIDER);
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
                result.setRequiresAuthorization(true);
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
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now());
        result.setTransactionReference("INT-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancel(InternalTransferCancellationRequestDTO request) {
        log.info("Cancelling internal transfer: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValueTransfer(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.INTERNAL_PROVIDER,
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

        // Process the cancellation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.CANCELLED);
        result.setCancellationReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setCancellationTimestamp(LocalDateTime.now());
        result.setFeesCharged(false);
        result.setFullyCancelled(true);
        result.setFundsReturned(true);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(InternalTransferRequestDTO request,
                                                 String executionDate,
                                                 String recurrencePattern) {
        log.info("Scheduling internal transfer: {}, execution date: {}, recurrence: {}",
                request, executionDate, recurrencePattern);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setStatus(PaymentStatus.SCHEDULED);
        result.setProvider(PaymentProviderType.INTERNAL_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setScheduledDate(LocalDate.parse(executionDate));
        result.setScheduleReference("SCH-" + UUID.randomUUID().toString().substring(0, 8));
        result.setScheduledTimestamp(LocalDateTime.now());
        result.setModifiable(true);
        result.setCancellable(true);
        result.setRecurrencePattern(recurrencePattern);

        return Mono.just(result);
    }

    private String generatePaymentId() {
        return "INT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for the payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(InternalTransferRequestDTO request) {
        // Implement SCA requirement logic based on various factors
        // For example, require SCA for payments above a certain amount
        if (request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            return true;
        }

        // For simulation purposes, we'll require SCA for 50% of payments randomly
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
        return "+1234567890";
    }

    /**
     * Determines if a transfer is considered high-value based on its ID.
     * In a real implementation, this would look up the transfer details.
     *
     * @param transferId The transfer ID
     * @return true if it's a high-value transfer requiring SCA for cancellation
     */
    private boolean isHighValueTransfer(String transferId) {
        // For simulation purposes, we'll require SCA for 50% of cancellations randomly
        return Math.random() > 0.5;
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(InternalTransferCancellationRequestDTO request) {
        log.info("Simulating internal transfer cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValueTransfer(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.INTERNAL_PROVIDER,
                "USD",
                scaRequired,
                true); // Internal transfers are generally cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
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

    @Override
    public Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId) {
        log.info("Triggering SCA for internal transfer: recipient={}, method={}, reference={}",
                maskPhoneNumber(recipientIdentifier), method, referenceId);

        // Delegate to the SCA provider
        return scaProvider.triggerSca(recipientIdentifier, method, referenceId);
    }

    @Override
    public Mono<ScaResultDTO> validateSca(ScaDTO sca) {
        log.info("Validating SCA for internal transfer: challengeId={}", sca.getChallengeId());

        // Delegate to the SCA provider
        return scaProvider.validateSca(sca);
    }

    @Override
    public Mono<Boolean> isHealthy() {
        // Perform health check for internal transfer provider
        // This could include checking connectivity to internal account systems
        log.debug("Performing health check for internal transfer provider");

        // For demonstration, we'll return a healthy status
        // In a real implementation, this would check connectivity to internal systems
        return Mono.just(true);
    }
}
