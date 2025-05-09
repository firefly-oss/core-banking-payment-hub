package com.catalis.core.banking.payments.hub.interfaces.providers;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.Target2CancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.Target2PaymentRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for TARGET2 payment operations.
 * Implementations of this interface will handle TARGET2 payment processing.
 */
public interface Target2PaymentProvider {

    /**
     * Simulates a TARGET2 payment without actual execution.
     *
     * @param request The TARGET2 payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulate(Target2PaymentRequestDTO request);

    /**
     * Executes a TARGET2 payment.
     *
     * @param request The TARGET2 payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> execute(Target2PaymentRequestDTO request);

    /**
     * Cancels an existing TARGET2 payment.
     *
     * @param paymentId The ID of the payment to cancel
     * @param reason The reason for cancellation
     * @return A Mono emitting the cancellation result
     * @deprecated Use {@link #cancel(Target2CancellationRequestDTO)} instead
     */
    @Deprecated
    Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason);

    /**
     * Cancels an existing TARGET2 payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(Target2CancellationRequestDTO request);

    /**
     * Simulates the cancellation of a TARGET2 payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(Target2CancellationRequestDTO request);

    /**
     * Schedules a TARGET2 payment for future execution.
     *
     * @param request The TARGET2 payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedule(Target2PaymentRequestDTO request, String executionDate);
}
