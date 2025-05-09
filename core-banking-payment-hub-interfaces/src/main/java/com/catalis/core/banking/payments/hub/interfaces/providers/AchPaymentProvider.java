package com.catalis.core.banking.payments.hub.interfaces.providers;

import com.catalis.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for ACH payment operations.
 * Implementations of this interface will handle ACH payment processing
 * for US bank transfers.
 */
public interface AchPaymentProvider {

    /**
     * Simulates an ACH payment without actual execution.
     *
     * @param request The ACH payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulate(AchTransferRequestDTO request);

    /**
     * Executes an ACH payment.
     *
     * @param request The ACH payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> execute(AchTransferRequestDTO request);

    /**
     * Cancels an existing ACH payment.
     *
     * @param paymentId The ID of the payment to cancel
     * @param reason The reason for cancellation
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason);

    /**
     * Schedules an ACH payment for future execution.
     *
     * @param request The ACH payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @param recurrencePattern Optional recurrence pattern for recurring payments
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedule(AchTransferRequestDTO request, 
                                           String executionDate, 
                                           String recurrencePattern);
}
