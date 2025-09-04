/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.core.utils.MetricsUtils;
import com.firefly.core.banking.payments.hub.core.utils.ScaUtils;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardScheduleRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.firefly.core.banking.payments.hub.interfaces.providers.CardPaymentProvider;
import com.firefly.core.banking.payments.hub.interfaces.providers.ScaProvider;
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
 * Default implementation of the CardPaymentProvider interface.
 * Acts as the card authorization center for all card payment operations.
 * Extends the AbstractBasePaymentProvider for standardized SCA handling and metrics.
 */
@Slf4j
@Component
public class DefaultCardPaymentProvider extends AbstractBasePaymentProvider implements CardPaymentProvider {

    @Autowired
    public DefaultCardPaymentProvider(ScaProvider scaProvider) {
        super(scaProvider);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(CardPaymentRequestDTO request) {
        log.info("Simulating card payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.CARD_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now());
        result.setEstimatedSettlementDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate().plusDays(1) : LocalDate.now().plusDays(1));
        result.setEstimatedFee(calculateCardProcessingFee(request));
        result.setFeeCurrency(request.getCurrency());
        result.setFeasible(isCardPaymentFeasible(request));
        result.setSimulationReference("SIM-CARD-" + UUID.randomUUID().toString().substring(0, 8));

        // Handle SCA (Strong Customer Authentication) - required for card payments
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false);

        if (scaRequired) {
            result.setScaDeliveryTriggered(true);
            result.setScaDeliveryTimestamp(LocalDateTime.now());

            String scaMethod = request.getSca() != null && request.getSca().getMethod() != null ?
                    request.getSca().getMethod() : "SMS";
            String scaRecipient = request.getSca() != null && request.getSca().getRecipient() != null ?
                    request.getSca().getRecipient() : ScaUtils.maskPhoneNumber(getDefaultPhoneNumber(request));

            result.setScaDeliveryMethod(scaMethod);
            result.setScaDeliveryRecipient(scaRecipient);
            result.setScaExpiryTimestamp(LocalDateTime.now().plusMinutes(10)); // Card SCA expires faster

            ScaResultDTO scaResult = new ScaResultDTO();
            scaResult.setMethod(scaMethod);
            scaResult.setChallengeId("CHL-CARD-" + UUID.randomUUID().toString().substring(0, 8));
            scaResult.setVerificationTimestamp(null);
            scaResult.setAttemptCount(0);
            scaResult.setMaxAttempts(3);
            scaResult.setExpired(false);
            scaResult.setExpiryTimestamp(result.getScaExpiryTimestamp());
            scaResult.setSuccess(false);

            result.setScaResult(scaResult);

            if (request.getSca() != null && request.getSca().getAuthenticationCode() != null) {
                ScaResultDTO validationResult = validateScaSync(request.getSca());
                result.setScaResult(validationResult);
                result.setScaCompleted(validationResult.isSuccess());
            }

            log.info("SCA delivery triggered for card payment simulation: method={}, recipient={}, expiryTime={}",
                    scaMethod, scaRecipient, result.getScaExpiryTimestamp());
        }

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(CardPaymentRequestDTO request) {
        log.info("Executing card payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setProvider(PaymentProviderType.CARD_PROVIDER);
        result.setTimestamp(LocalDateTime.now());

        // Validate card details first
        if (!isCardValid(request)) {
            result.setSuccess(false);
            result.setStatus(PaymentStatus.REJECTED);
            result.setErrorCode("INVALID_CARD");
            result.setErrorMessage("Card validation failed");
            return Mono.just(result);
        }

        // Handle SCA for card authorization
        boolean scaRequired = isScaRequired(request);
        result.setScaRequired(scaRequired);
        result.setScaCompleted(false);

        if (scaRequired) {
            if (request.getSca() == null) {
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_REQUIRED");
                result.setErrorMessage("Strong Customer Authentication is required for card payment authorization");
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }

            ScaResultDTO scaResult = validateScaSync(request.getSca());
            result.setScaResult(scaResult);
            result.setScaCompleted(scaResult.isSuccess());

            if (!scaResult.isSuccess()) {
                result.setSuccess(false);
                result.setStatus(PaymentStatus.REJECTED);
                result.setErrorCode("SCA_FAILED");
                result.setErrorMessage("Card payment authorization failed: " + scaResult.getMessage());
                result.setRequiresAuthorization(true);
                return Mono.just(result);
            }
        }

        // Card authorization successful
        result.setSuccess(true);
        result.setStatus(PaymentStatus.COMPLETED);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now().plusDays(1));
        result.setTransactionReference("TRN-CARD-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("AUTH-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);

        log.info("Card payment authorized successfully: paymentId={}, authCode={}", 
                result.getPaymentId(), result.getClearingSystemReference());

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(CardCancellationRequestDTO request) {
        log.info("Simulating card payment cancellation: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(request.getPaymentId());
        result.setRequestId(request.getPaymentId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.CARD_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setFeasible(true);
        result.setSimulationReference("SIM-CANCEL-" + UUID.randomUUID().toString().substring(0, 8));

        // Card cancellations typically require SCA
        result.setScaRequired(true);
        result.setScaCompleted(false);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(CardCancellationRequestDTO request) {
        log.info("Cancelling card payment: {}", request);

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(request.getPaymentId());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setProvider(PaymentProviderType.CARD_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setStatus(PaymentStatus.CANCELLED);
        result.setCancellationReference("CANC-CARD-" + UUID.randomUUID().toString().substring(0, 8));

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(CardScheduleRequestDTO request) {
        log.info("Scheduling card payment: {}", request);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setProvider(PaymentProviderType.CARD_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setStatus(PaymentStatus.SCHEDULED);

        return Mono.just(result);
    }

    private String generatePaymentId() {
        return "CARD-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isScaRequired(CardPaymentRequestDTO request) {
        // Card payments typically require SCA for amounts over 30 EUR or for high-risk transactions
        return request.getAmount().compareTo(new BigDecimal("30.00")) > 0 || isHighRiskTransaction(request);
    }

    private boolean isHighRiskTransaction(CardPaymentRequestDTO request) {
        // Simple risk assessment based on amount and merchant category
        return request.getAmount().compareTo(new BigDecimal("500.00")) > 0 ||
               "HIGH_RISK".equals(request.getMerchantCategoryCode());
    }

    private boolean isCardValid(CardPaymentRequestDTO request) {
        // Basic card validation (in real implementation, would use proper validation)
        return request.getCardNumber() != null && 
               request.getCardNumber().length() >= 13 &&
               request.getCardholderName() != null &&
               request.getExpiryMonth() != null &&
               request.getExpiryYear() != null;
    }

    private boolean isCardPaymentFeasible(CardPaymentRequestDTO request) {
        // Check if card payment is feasible based on card validation and limits
        return isCardValid(request) && 
               request.getAmount().compareTo(new BigDecimal("10000.00")) <= 0; // Max 10k limit
    }

    private BigDecimal calculateCardProcessingFee(CardPaymentRequestDTO request) {
        // Calculate processing fee based on amount and card brand
        BigDecimal baseRate = new BigDecimal("0.029"); // 2.9%
        BigDecimal fixedFee = new BigDecimal("0.30");
        return request.getAmount().multiply(baseRate).add(fixedFee);
    }

    private ScaResultDTO validateScaSync(ScaDTO sca) {
        ScaResultDTO result = new ScaResultDTO();
        result.setMethod(sca.getMethod());
        result.setChallengeId(sca.getChallengeId());
        result.setVerificationTimestamp(LocalDateTime.now());
        result.setAttemptCount(1);
        result.setMaxAttempts(3);
        result.setExpired(false);
        result.setExpiryTimestamp(LocalDateTime.now().plusMinutes(10));
        
        // Simple validation - in real implementation would validate against stored code
        boolean isValid = "123456".equals(sca.getAuthenticationCode());
        result.setSuccess(isValid);
        result.setMessage(isValid ? "Authentication successful" : "Invalid authentication code");
        
        return result;
    }

    @Override
    protected Mono<Boolean> checkProviderHealth() {
        // Health check for card processor
        log.debug("Checking card payment provider health");
        return Mono.just(true)
                .delayElement(Duration.ofMillis(100));
    }

    @Override
    protected String getProviderName() {
        return "DefaultCardPaymentProvider";
    }

    private String getDefaultPhoneNumber(CardPaymentRequestDTO request) {
        // In real implementation, would retrieve from customer profile
        return "+1234567890";
    }
}