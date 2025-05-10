package com.catalis.core.banking.payments.hub.interfaces.providers;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftScheduleRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for SWIFT payment operations.
 * Implementations of this interface will handle SWIFT payment processing
 * for different payment processors (e.g., Treezor, Iberpay).
 */
public interface SwiftPaymentProvider extends BasePaymentProvider {

    /**
     * Simulates a SWIFT payment without actual execution.
     *
     * @param request The SWIFT payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulate(SwiftPaymentRequestDTO request);

    /**
     * Executes a SWIFT payment.
     *
     * @param request The SWIFT payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> execute(SwiftPaymentRequestDTO request);

    /**
     * Simulates the cancellation of a SWIFT payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(SwiftCancellationRequestDTO request);

    /**
     * Cancels an existing SWIFT payment.
     *
     * @param request The cancellation request
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(SwiftCancellationRequestDTO request);

    /**
     * Schedules a SWIFT payment for future execution.
     *
     * @param request The schedule request
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedule(SwiftScheduleRequestDTO request);
}