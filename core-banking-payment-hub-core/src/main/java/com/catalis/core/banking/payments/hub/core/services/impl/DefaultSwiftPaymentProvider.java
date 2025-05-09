package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftScheduleRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.providers.SwiftPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the SwiftPaymentProvider interface.
 */
@Component
public class DefaultSwiftPaymentProvider implements SwiftPaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultSwiftPaymentProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(SwiftPaymentRequestDTO request) {
        log.info("Simulating SWIFT payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.SWIFT_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now().plusDays(1));
        result.setEstimatedSettlementDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate().plusDays(2) : LocalDate.now().plusDays(3));
        result.setEstimatedFee(new BigDecimal("25.00"));
        result.setFeeCurrency(request.getCurrency());
        result.setEstimatedExchangeRate(new BigDecimal("1.1050"));
        result.setFeasible(true);

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired && request.getSca() != null) {
            ScaResultDTO scaResult = validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(SwiftPaymentRequestDTO request) {
        log.info("Executing SWIFT payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.SWIFT_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired) {
            if (request.getSca() == null) {
                // SCA is required but not provided
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required for this payment");
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }

            ScaResultDTO scaResult = validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                // SCA validation failed
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getErrorMessage());
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now().plusDays(2));
        result.setTransactionReference("TRN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("CSR-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getSenderReference());

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(SwiftCancellationRequestDTO request) {
        log.info("Cancelling SWIFT payment: {}", request);

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(request.getPaymentId());
        result.setRequestId(UUID.randomUUID().toString());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setProvider(PaymentProviderType.SWIFT_PROVIDER);
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

            ScaResultDTO scaResult = validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                // SCA validation failed
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getErrorMessage());
                return Mono.just(result);
            }
        }

        // If we get here, either SCA is not required or it passed validation
        result.setSuccess(true);
        result.setStatus(PaymentStatus.CANCELLED);
        result.setCancellationReason(request.getCancellationReason());
        result.setCancellationReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setCancellationTimestamp(LocalDateTime.now());
        result.setFeesCharged(true);
        result.setFullyCancelled(true);
        result.setFundsReturned(true);
        result.setProviderReference(request.getOriginalTransactionReference());

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(SwiftScheduleRequestDTO request) {
        log.info("Scheduling SWIFT payment: {}", request);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getPaymentRequest().getRequestId());
        result.setPaymentType(request.getPaymentRequest().getPaymentType());
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.SWIFT_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request.getPaymentRequest());
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired) {
            if (request.getPaymentRequest().getSca() == null) {
                // SCA is required but not provided
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required to schedule this payment");
                return Mono.just(result);
            }

            ScaResultDTO scaResult = validateSca(request.getPaymentRequest().getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                // SCA validation failed
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getErrorMessage());
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
    private boolean isScaRequired(SwiftPaymentRequestDTO request) {
        // Implement SCA requirement logic based on various factors
        // For example, require SCA for payments above a certain amount
        if (request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            return true;
        }

        // Or for payments to certain countries
        if (request.getBeneficiaryInstitution() != null &&
            request.getBeneficiaryInstitution().getBankCountryCode() != null) {
            String countryCode = request.getBeneficiaryInstitution().getBankCountryCode();
            if (!"US".equals(countryCode) && !"CA".equals(countryCode)) {
                return true;
            }
        }

        // For simulation purposes, we'll require SCA for 50% of payments randomly
        return Math.random() > 0.5;
    }

    /**
     * Validates the provided SCA information.
     *
     * @param sca The SCA information to validate
     * @return The validation result
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
        } else if (sca.getAuthenticationCode() == null) {
            result.setSuccess(false);
            result.setErrorCode("SCA_CODE_MISSING");
            result.setErrorMessage("Authentication code is required");
        } else {
            // Random success/failure for other codes
            boolean success = Math.random() > 0.3; // 70% success rate
            result.setSuccess(success);
            if (!success) {
                result.setErrorCode("SCA_INVALID_CODE");
                result.setErrorMessage("Invalid authentication code");
            }
        }

        return result;
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
     * Validates the provided SCA information for a cancellation request.
     *
     * @param sca The SCA information to validate
     * @return The validation result
     */
    private ScaResultDTO validateCancellationSca(ScaDTO sca) {
        // For simplicity, we'll use the same validation logic as for payments
        return validateSca(sca);
    }
}
