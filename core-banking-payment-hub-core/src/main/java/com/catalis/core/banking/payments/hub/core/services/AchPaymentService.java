package com.catalis.core.banking.payments.hub.core.services;

import com.catalis.core.banking.payments.hub.interfaces.dtos.ach.AchCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import reactor.core.publisher.Mono;

/**
 * Service interface for ACH payment operations.
 */
public interface AchPaymentService {

    /**
     * Simulates an ACH payment without actual execution.
     *
     * @param request The ACH payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulatePayment(AchTransferRequestDTO request);

    /**
     * Executes an ACH payment.
     *
     * @param request The ACH payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executePayment(AchTransferRequestDTO request);

    /**
     * Cancels an existing ACH payment.
     *
     * @param paymentId The ID of the payment to cancel
     * @param reason The reason for cancellation
     * @return A Mono emitting the cancellation result
     * @deprecated Use {@link #cancelPayment(AchCancellationRequestDTO)} instead
     */
    @Deprecated
    Mono<PaymentCancellationResultDTO> cancelPayment(String paymentId, String reason);

    /**
     * Cancels an existing ACH payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelPayment(AchCancellationRequestDTO request);

    /**
     * Simulates the cancellation of an ACH payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(AchCancellationRequestDTO request);

    /**
     * Schedules an ACH payment for future execution.
     *
     * @param request The ACH payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @param recurrencePattern Optional recurrence pattern for recurring payments
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedulePayment(AchTransferRequestDTO request,
                                                 String executionDate,
                                                 String recurrencePattern);
}
