package com.catalis.core.banking.payments.hub.core.services;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import reactor.core.publisher.Mono;

/**
 * Generic interface for payment operations.
 * Defines common methods for all payment types.
 *
 * @param <T> The payment request type
 * @param <C> The cancellation request type
 * @param <S> The schedule request type
 */
public interface PaymentService<T, C, S> {

    /**
     * Simulates a payment without actual execution.
     *
     * @param request The payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulatePayment(T request);

    /**
     * Executes a payment.
     *
     * @param request The payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executePayment(T request);

    /**
     * Cancels an existing payment.
     *
     * @param request The cancellation request
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelPayment(C request);

    /**
     * Schedules a payment for future execution.
     *
     * @param request The schedule request
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedulePayment(S request);
}