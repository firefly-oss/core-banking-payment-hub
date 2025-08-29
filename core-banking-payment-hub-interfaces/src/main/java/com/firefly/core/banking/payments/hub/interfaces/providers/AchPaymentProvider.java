package com.firefly.core.banking.payments.hub.interfaces.providers;

import com.firefly.core.banking.payments.hub.interfaces.dtos.ach.AchCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for ACH payment operations.
 * Implementations of this interface will handle ACH payment processing
 * for US bank transfers.
 */
public interface AchPaymentProvider extends BasePaymentProvider {

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
     * Cancels an existing ACH payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(AchCancellationRequestDTO request);

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
    Mono<PaymentScheduleResultDTO> schedule(AchTransferRequestDTO request,
                                           String executionDate,
                                           String recurrencePattern);
}
