package com.firefly.core.banking.payments.hub.core.utils;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for payment cancellation operations.
 * This class provides common functionality used across different payment providers.
 */
public class CancellationUtils {

    private static final Logger log = LoggerFactory.getLogger(CancellationUtils.class);

    /**
     * Creates a default cancellation simulation result.
     *
     * @param paymentId The ID of the payment to cancel
     * @param paymentType The type of payment
     * @param providerType The payment provider type
     * @param feeCurrency The currency for the cancellation fee
     * @param scaRequired Whether SCA is required for the cancellation
     * @param feasible Whether the cancellation is feasible
     * @return A default cancellation simulation result
     */
    public static PaymentSimulationResultDTO createCancellationSimulationResult(
            String paymentId,
            PaymentType paymentType,
            PaymentProviderType providerType,
            String feeCurrency,
            boolean scaRequired,
            boolean feasible) {

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(paymentId);
        result.setRequestId(UUID.randomUUID().toString());
        result.setPaymentType(paymentType);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(providerType);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now());
        result.setEstimatedSettlementDate(LocalDate.now());
        result.setEstimatedFee(new BigDecimal("5.00"));
        result.setFeeCurrency(feeCurrency);
        result.setFeasible(feasible);
        result.setSimulationReference(ScaUtils.generateSimulationReference());

        // Handle SCA (Strong Customer Authentication)
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        return result;
    }

    /**
     * Sets up SCA delivery information in the simulation result.
     *
     * @param result The simulation result to update
     * @param sca The SCA information from the request
     * @param defaultPhoneNumber The default phone number to use if not provided in the request
     */
    public static void setupScaDelivery(
            PaymentSimulationResultDTO result,
            ScaDTO sca,
            String defaultPhoneNumber) {

        result.setScaDeliveryTriggered(true);
        result.setScaDeliveryTimestamp(LocalDateTime.now());

        // Determine SCA method and recipient
        String scaMethod = sca != null && sca.getMethod() != null ?
                sca.getMethod() : "SMS"; // Default to SMS if not specified
        String scaRecipient = sca != null && sca.getRecipient() != null ?
                sca.getRecipient() : ScaUtils.maskPhoneNumber(defaultPhoneNumber);

        result.setScaDeliveryMethod(scaMethod);
        result.setScaDeliveryRecipient(scaRecipient);
        result.setScaExpiryTimestamp(LocalDateTime.now().plusMinutes(15)); // SCA code valid for 15 minutes

        // Create SCA result with challenge information
        ScaResultDTO scaResult = ScaUtils.createDefaultScaResult(scaMethod, result.getScaExpiryTimestamp());
        result.setScaResult(scaResult);

        // If SCA code is already provided in the request, validate it
        if (sca != null && sca.getAuthenticationCode() != null) {
            ScaResultDTO validationResult = ScaUtils.validateSca(sca);
            result.setScaResult(validationResult);
            result.setScaCompleted(validationResult.isSuccess());
        }

        log.info("SCA delivery triggered for cancellation simulation: method={}, recipient={}, expiryTime={}",
                scaMethod, scaRecipient, result.getScaExpiryTimestamp());
    }

    /**
     * Creates a default cancellation result.
     *
     * @param paymentId The ID of the payment to cancel
     * @param paymentType The type of payment
     * @param providerType The payment provider type
     * @param cancellationReason The reason for cancellation
     * @param scaRequired Whether SCA is required for the cancellation
     * @return A default cancellation result
     */
    public static PaymentCancellationResultDTO createCancellationResult(
            String paymentId,
            PaymentType paymentType,
            PaymentProviderType providerType,
            String cancellationReason,
            boolean scaRequired) {

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(paymentId);
        result.setRequestId(UUID.randomUUID().toString());
        result.setPaymentType(paymentType);
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setProvider(providerType);
        result.setTimestamp(LocalDateTime.now());
        result.setCancellationReason(cancellationReason);

        // For cancellation, we'll require SCA for high-value payments
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        return result;
    }

    /**
     * Validates SCA for cancellation and updates the result accordingly.
     *
     * @param result The cancellation result to update
     * @param sca The SCA information to validate
     * @return true if SCA validation was successful, false otherwise
     */
    public static boolean validateScaForCancellation(
            PaymentCancellationResultDTO result,
            ScaDTO sca) {

        if (sca == null) {
            // SCA is required but not provided
            result.setSuccess(false);
            result.setStatus(PaymentStatus.REJECTED);
            result.setErrorCode("SCA_REQUIRED");
            result.setErrorMessage("Strong Customer Authentication is required to cancel this payment");
            return false;
        }

        ScaResultDTO scaResult = ScaUtils.validateSca(sca);
        result.setScaResult(scaResult);
        result.setScaCompleted(scaResult.isSuccess());

        if (!scaResult.isSuccess()) {
            // SCA validation failed
            result.setSuccess(false);
            result.setStatus(PaymentStatus.REJECTED);
            result.setErrorCode("SCA_FAILED");
            result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Sets the cancellation result to indicate that cancellation is not supported.
     *
     * @param result The cancellation result to update
     * @param paymentType The type of payment
     */
    public static void setCancellationNotSupported(
            PaymentCancellationResultDTO result,
            String paymentType) {

        result.setSuccess(false);
        result.setStatus(PaymentStatus.REJECTED);
        result.setErrorCode("CANCELLATION_NOT_SUPPORTED");
        result.setErrorMessage("Cancellation is not supported for " + paymentType + " payments once they have been submitted");
    }
}
