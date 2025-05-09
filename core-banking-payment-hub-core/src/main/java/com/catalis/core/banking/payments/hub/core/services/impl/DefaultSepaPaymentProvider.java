package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.catalis.core.banking.payments.hub.interfaces.providers.SepaPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the SepaPaymentProvider interface.
 */
@Component
public class DefaultSepaPaymentProvider implements SepaPaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultSepaPaymentProvider.class);

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
        result.setStatus(PaymentStatus.COMPLETED);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
        result.setExecutionDate(LocalDate.now());
        result.setExpectedSettlementDate(LocalDate.now().plusDays(1));
        result.setTransactionReference("TRN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setClearingSystemReference("CSR-" + UUID.randomUUID().toString().substring(0, 8));
        result.setReceivedTimestamp(LocalDateTime.now());
        result.setRequiresAuthorization(false);

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
        result.setStatus(PaymentStatus.CANCELLED);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
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
        result.setStatus(PaymentStatus.SCHEDULED);
        result.setProvider(PaymentProviderType.SEPA_PROVIDER);
        result.setTimestamp(LocalDateTime.now());
        result.setSuccess(true);
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
}
