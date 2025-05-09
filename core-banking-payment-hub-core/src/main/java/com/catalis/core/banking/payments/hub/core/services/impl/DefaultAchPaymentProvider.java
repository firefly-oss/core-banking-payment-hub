package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.providers.AchPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the AchPaymentProvider interface.
 * Handles ACH payments for US bank transfers.
 */
@Component
public class DefaultAchPaymentProvider implements AchPaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultAchPaymentProvider.class);

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(AchTransferRequestDTO request) {
        log.info("Simulating ACH payment: {}", request);

        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SIMULATE);
        result.setStatus(PaymentStatus.VALIDATED);
        result.setProvider(PaymentProviderType.ACH_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setEstimatedExecutionDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate() : LocalDate.now().plusDays(1));
        result.setEstimatedSettlementDate(request.getRequestedExecutionDate() != null ?
                request.getRequestedExecutionDate().plusDays(1) : LocalDate.now().plusDays(2));
        result.setEstimatedFee(new BigDecimal("1.50"));
        result.setFeeCurrency("USD");
        result.setFeasible(true);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(AchTransferRequestDTO request) {
        log.info("Executing ACH payment: {}", request);

        PaymentExecutionResultDTO result = new PaymentExecutionResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.EXECUTE);
        result.setStatus(PaymentStatus.COMPLETED);
        result.setProvider(PaymentProviderType.ACH_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now().plusDays(1));
        result.setTransactionReference("ACH-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference(request.getReceivingBankRoutingNumber() + "-" + 
                UUID.randomUUID().toString().substring(0, 4));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason) {
        log.info("Cancelling ACH payment: {}, reason: {}", paymentId, reason);

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(paymentId);
        result.setRequestId(UUID.randomUUID().toString());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setStatus(PaymentStatus.CANCELLED);
        result.setProvider(PaymentProviderType.ACH_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setCancellationReason(reason);
        result.setCancellationReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setCancellationTimestamp(LocalDateTime.now());
        result.setFeesCharged(false);
        result.setFullyCancelled(true);
        result.setFundsReturned(true);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedule(AchTransferRequestDTO request, 
                                                 String executionDate, 
                                                 String recurrencePattern) {
        log.info("Scheduling ACH payment: {}, execution date: {}, recurrence: {}", 
                request, executionDate, recurrencePattern);

        PaymentScheduleResultDTO result = new PaymentScheduleResultDTO();
        result.setPaymentId(generatePaymentId());
        result.setRequestId(request.getRequestId());
        result.setPaymentType(request.getPaymentType());
        result.setOperationType(PaymentOperationType.SCHEDULE);
        result.setStatus(PaymentStatus.SCHEDULED);
        result.setProvider(PaymentProviderType.ACH_PROVIDER);
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
        return "ACH-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
