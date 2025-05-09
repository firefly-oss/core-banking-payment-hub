package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.CancellationUtils;
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
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentType;
import com.catalis.core.banking.payments.hub.interfaces.providers.UkPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the UkPaymentProvider interface.
 * Provides simulation functionality for UK payment types.
 */
@Service
public class DefaultUkPaymentProvider implements UkPaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultUkPaymentProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulateFasterPayment(UkFasterPaymentRequestDTO request) {
        log.info("Simulating UK Faster Payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_FPS);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now());
        result.setEstimatedSettlementDate(LocalDate.now());
        result.setEstimatedFee(new BigDecimal("0.20"));
        result.setFeeCurrency(request.getCurrency());
        result.setEstimatedExchangeRate(null); // No exchange rate for domestic payments
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

            log.info("SCA delivery triggered for UK Faster Payment simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeFasterPayment(UkFasterPaymentRequestDTO request) {
        log.info("Executing UK Faster Payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_FPS);
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
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
        result.setTransactionReference("FPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("FPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getEndToEndId());

        return Mono.just(result);
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelFasterPayment(UkCancellationRequestDTO request) {
        log.info("Cancelling UK Faster Payment: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.UK_PROVIDER,
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
        // However, UK Faster Payments are generally not cancellable once submitted
        CancellationUtils.setCancellationNotSupported(result, "UK Faster Payment");

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateFasterPaymentCancellation(UkCancellationRequestDTO request) {
        log.info("Simulating UK Faster Payment cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.UK_PROVIDER,
                "GBP",
                scaRequired,
                false); // UK Faster Payments are generally not cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleFasterPayment(UkFasterPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling UK Faster Payment: {}, execution date: {}", request, executionDate);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_FPS);
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
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
        result.setTransactionReference("FPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateBacsPaymentCancellation(UkCancellationRequestDTO request) {
        log.info("Simulating UK BACS Payment cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.UK_PROVIDER,
                "GBP",
                scaRequired,
                true); // UK BACS Payments are generally cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateBacsPayment(UkBacsPaymentRequestDTO request) {
        log.info("Simulating UK BACS Payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_BACS);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now().plusDays(1));
        result.setEstimatedSettlementDate(LocalDate.now().plusDays(3));
        result.setEstimatedFee(new BigDecimal("0.10"));
        result.setFeeCurrency(request.getCurrency());
        result.setEstimatedExchangeRate(null); // No exchange rate for domestic payments
        result.setFeasible(true);

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired && request.getSca() != null) {
            ScaResultDTO scaResult = ScaUtils.validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeBacsPayment(UkBacsPaymentRequestDTO request) {
        log.info("Executing UK BACS Payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_BACS);
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
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
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now().plusDays(2));
        result.setTransactionReference("BACS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("BACS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getServiceUserNumber());

        return Mono.just(result);
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelBacsPayment(UkCancellationRequestDTO request) {
        log.info("Cancelling UK BACS Payment: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.UK_PROVIDER,
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
    public Mono<PaymentScheduleResultDTO> scheduleBacsPayment(UkBacsPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling UK BACS Payment: {}, execution date: {}", request, executionDate);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_BACS);
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
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
        result.setExpectedSettlementDate(LocalDate.parse(executionDate).plusDays(2));
        result.setTransactionReference("BACS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateChapsPayment(UkChapsPaymentRequestDTO request) {
        log.info("Simulating UK CHAPS Payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_CHAPS);
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(LocalDate.now());
        result.setEstimatedSettlementDate(LocalDate.now());
        result.setEstimatedFee(new BigDecimal("25.00"));
        result.setFeeCurrency(request.getCurrency());
        result.setEstimatedExchangeRate(null); // No exchange rate for domestic payments
        result.setFeasible(true);

        // Handle SCA (Strong Customer Authentication)
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false); // Initially not completed

        if (scaRequired && request.getSca() != null) {
            ScaResultDTO scaResult = ScaUtils.validateSca(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executeChapsPayment(UkChapsPaymentRequestDTO request) {
        log.info("Executing UK CHAPS Payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_CHAPS);
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
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
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now());
        result.setTransactionReference("CHAPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("CHAPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);
        result.setProviderReference(request.getEndToEndId());

        return Mono.just(result);
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelChapsPayment(UkCancellationRequestDTO request) {
        log.info("Cancelling UK CHAPS Payment: {}", request);

        // Create a default cancellation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.UK_PROVIDER,
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
        // However, UK CHAPS Payments are generally not cancellable once submitted
        CancellationUtils.setCancellationNotSupported(result, "UK CHAPS Payment");

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateChapsPaymentCancellation(UkCancellationRequestDTO request) {
        log.info("Simulating UK CHAPS Payment cancellation: {}", request);

        // Create a default cancellation simulation result
        boolean scaRequired = isHighValuePayment(request.getPaymentId());
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                request.getPaymentId(),
                request.getPaymentType(),
                PaymentProviderType.UK_PROVIDER,
                "GBP",
                scaRequired,
                false); // UK CHAPS Payments are generally not cancellable

        // Set up SCA delivery if required
        if (scaRequired) {
            CancellationUtils.setupScaDelivery(result, request.getSca(), getDefaultPhoneNumber(request));
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> scheduleChapsPayment(UkChapsPaymentRequestDTO request, String executionDate) {
        log.info("Scheduling UK CHAPS Payment: {}, execution date: {}", request, executionDate);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(PaymentType.UK_CHAPS);
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.UK_PROVIDER);
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
        result.setTransactionReference("CHAPS-" + UUID.randomUUID().toString().substring(0, 8));
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    /**
     * Generates a unique payment ID.
     *
     * @return A unique payment ID
     */
    private String generatePaymentId() {
        return "UK-" + UUID.randomUUID().toString();
    }

    /**
     * Determines if Strong Customer Authentication (SCA) is required for the payment.
     *
     * @param request The payment request
     * @return true if SCA is required, false otherwise
     */
    private boolean isScaRequired(Object request) {
        // In a real implementation, this would check various factors like payment amount, risk, etc.
        // For this simulation, we'll require SCA for all payments
        return true;
    }

    // Using ScaUtils.validateSca() instead



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
        return "+44123456789";
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


}
