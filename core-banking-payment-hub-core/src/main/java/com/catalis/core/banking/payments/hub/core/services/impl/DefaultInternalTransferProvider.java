package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.providers.InternalTransferProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the InternalTransferProvider interface.
 * Handles transfers between accounts within the core banking system.
 */
@Component
public class DefaultInternalTransferProvider implements InternalTransferProvider {

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
        result.setStatus(PaymentStatus.COMPLETED);
        result.setProvider(PaymentProviderType.INTERNAL_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now());
        result.setTransactionReference("INT-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);

        return Mono.just(result);
    }

    @Override
    public Mono<PaymentCancellationResultDTO> cancel(String transferId, String reason) {
        log.info("Cancelling internal transfer: {}, reason: {}", transferId, reason);

        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setPaymentId(transferId);
        result.setRequestId(UUID.randomUUID().toString());
        result.setOperationType(PaymentOperationType.CANCEL);
        result.setStatus(PaymentStatus.CANCELLED);
        result.setProvider(PaymentProviderType.INTERNAL_PROVIDER);
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
}
