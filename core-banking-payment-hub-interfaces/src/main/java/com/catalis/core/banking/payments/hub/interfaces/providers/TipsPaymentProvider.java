package com.catalis.core.banking.payments.hub.interfaces.providers;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.TipsCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.TipsPaymentRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for TIPS (TARGET Instant Payment Settlement) payment operations.
 * Implementations of this interface will handle TIPS payment processing.
 */
public interface TipsPaymentProvider {

    /**
     * Simulates a TIPS payment without actual execution.
     *
     * @param request The TIPS payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulate(TipsPaymentRequestDTO request);

    /**
     * Executes a TIPS payment.
     *
     * @param request The TIPS payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> execute(TipsPaymentRequestDTO request);

    /**
     * Cancels an existing TIPS payment.
     *
     * @param paymentId The ID of the payment to cancel
     * @param reason The reason for cancellation
     * @return A Mono emitting the cancellation result
     * @deprecated Use {@link #cancel(TipsCancellationRequestDTO)} instead
     */
    @Deprecated
    Mono<PaymentCancellationResultDTO> cancel(String paymentId, String reason);

    /**
     * Cancels an existing TIPS payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(TipsCancellationRequestDTO request);

    /**
     * Simulates the cancellation of a TIPS payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(TipsCancellationRequestDTO request);

    /**
     * Schedules a TIPS payment for future execution.
     * Note: TIPS is primarily for instant payments, but this method is provided for completeness.
     *
     * @param request The TIPS payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedule(TipsPaymentRequestDTO request, String executionDate);
}
