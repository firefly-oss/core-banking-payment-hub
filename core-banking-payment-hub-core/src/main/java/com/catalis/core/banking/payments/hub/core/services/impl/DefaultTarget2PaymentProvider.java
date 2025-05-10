package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.CancellationUtils;
import com.catalis.core.banking.payments.hub.core.utils.MetricsUtils;
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
import com.catalis.core.banking.payments.hub.interfaces.providers.ScaProvider;
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
 * Default implementation of the Target2PaymentProvider interface.
 * Extends the AbstractBasePaymentProvider for standardized SCA handling and metrics.
 */
@Slf4j
@Component
public class DefaultTarget2PaymentProvider extends AbstractBasePaymentProvider implements Target2PaymentProvider {

    @Autowired
    public DefaultTarget2PaymentProvider(ScaProvider scaProvider) {
        super(scaProvider);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(Target2PaymentRequestDTO request) {
        log.info("Simulating TARGET2 payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.TARGET2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now());
        result.setEstimatedSettlementDate(result.getEstimatedExecutionDate().plusDays(1));
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
                    request.getSca().getRecipient() : ScaUtils.maskPhoneNumber(getDefaultPhoneNumber(request));

            result.setScaDeliveryMethod(scaMethod);
            result.setScaDeliveryRecipient(scaRecipient);
            result.setScaExpiryTimestamp(LocalDateTime.now().plusMinutes(15)); // SCA code valid for 15 minutes

            // Create SCA result with challenge information
            ScaResultDTO scaResult = ScaUtils.createDefaultScaResult(scaMethod, result.getScaExpiryTimestamp());
            result.setScaResult(scaResult);

            // If SCA code is already provided in the request, validate it
            if (request.getSca() != null && request.getSca().getAuthenticationCode() != null) {
                ScaResultDTO validationResult = validateScaSync(request.getSca());
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
        result.setPaymentType(request.getPaymentType());
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

    @Deprecated
    public Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason) {
        log.info("Cancelling TARGET2 payment (deprecated method): paymentId={}, reason={}", paymentId, reason);

        // Create a cancellation request DTO
        Target2CancellationRequestDTO request = new Target2CancellationRequestDTO();
        request.setPaymentId(paymentId);
        request.setCancellationReason(reason);
        request.setPaymentType(PaymentType.TARGET2);

        // Delegate to the new method
        return cancel(request);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(Target2CancellationRequestDTO request) {
        log.info("Cancelling TARGET2 payment: {}", request);

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(request.getPaymentId());
        result.setRequestId(UUID.randomUUID().toString());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setProvider(PaymentProviderType.TARGET2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // For cancellation, we'll require SCA for high-value payments
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

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
        result.setCancellationDate(LocalDate.now());
        result.setCancellationReason(request.getCancellationReason());
        result.setFullyCancelled(true);
        result.setFundsReturned(true);
        result.setCancellationReference("REF-" + UUID.randomUUID().toString().substring(0, 8));

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
                true); // TARGET2 payments are generally cancellable

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
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.TARGET2_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        // Check if a simulation reference is provided
        String simulationReference = request.getSimulationReference();
        if (simulationReference != null && !simulationReference.isEmpty()) {
            log.info("Using simulation reference for SCA validation in scheduling: {}", simulationReference);
            // In a real implementation, we would look up the simulation details using the reference
            // and validate the SCA code against the previously delivered code
        }

        if (scaRequired) {
            if (request.getSca() == null) {
                // SCA is required but not provided
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required to schedule this payment");
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
        result.setStatus(PaymentStatus.SCHEDULED);
        result.setScheduledExecutionDate(LocalDate.parse(executionDate));
        result.setExpectedSettlementDate(LocalDate.parse(executionDate).plusDays(1));
        result.setTransactionReference("SCH-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    /**
     * Generates a unique payment ID.
     *
     * @return A unique payment ID
     */
    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for a payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(Target2PaymentRequestDTO request) {
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
        return scaProvider.validateSca(sca).block();
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

    // SCA methods are now inherited from AbstractBasePaymentProvider

    @Override
    protected Mono<Boolean> checkProviderHealth() {
        // Perform health check for TARGET2 payment provider
        // This could include checking connectivity to external systems
        log.debug("Checking connectivity to TARGET2 payment systems");

        // For demonstration, we'll return a healthy status
        // In a real implementation, this would check connectivity to systems
        return Mono.just(true);
    }

    @Override
    protected String getProviderName() {
        return "target2";
    }
}
