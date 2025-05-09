package com.catalis.core.banking.payments.hub.core.services;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.EbaStep2CancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.EbaStep2PaymentRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Service interface for EBA STEP2 payment operations.
 */
public interface EbaStep2PaymentService {

    /**
     * Simulates an EBA STEP2 payment without actual execution.
     *
     * @param request The EBA STEP2 payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulatePayment(EbaStep2PaymentRequestDTO request);

    /**
     * Executes an EBA STEP2 payment.
     *
     * @param request The EBA STEP2 payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executePayment(EbaStep2PaymentRequestDTO request);

    /**
     * Cancels an existing EBA STEP2 payment.
     *
     * @param paymentId The ID of the payment to cancel
     * @param reason The reason for cancellation
     * @return A Mono emitting the cancellation result
     * @deprecated Use {@link #cancelPayment(EbaStep2CancellationRequestDTO)} instead
     */
    @Deprecated
    Mono<PaymentCancellationResultDTO> cancelPayment(String paymentId, String reason);

    /**
     * Cancels an existing EBA STEP2 payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelPayment(EbaStep2CancellationRequestDTO request);

    /**
     * Simulates the cancellation of an EBA STEP2 payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(EbaStep2CancellationRequestDTO request);

    /**
     * Schedules an EBA STEP2 payment for future execution.
     *
     * @param request The EBA STEP2 payment request to schedule
     * @param executionDate The date when the payment should be executed (YYYY-MM-DD)
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedulePayment(EbaStep2PaymentRequestDTO request, String executionDate);
}
