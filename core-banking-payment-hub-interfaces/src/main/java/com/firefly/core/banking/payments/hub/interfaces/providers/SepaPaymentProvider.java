package com.firefly.core.banking.payments.hub.interfaces.providers;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for SEPA payment operations.
 * Implementations of this interface will handle SEPA payment processing
 * for different payment processors (e.g., Treezor, Iberpay).
 */
public interface SepaPaymentProvider extends BasePaymentProvider {

    /**
     * Simulates a SEPA payment without actual execution.
     *
     * @param request The SEPA payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulate(SepaPaymentRequestDTO request);

    /**
     * Executes a SEPA payment.
     *
     * @param request The SEPA payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> execute(SepaPaymentRequestDTO request);

    /**
     * Simulates the cancellation of a SEPA payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(SepaCancellationRequestDTO request);

    /**
     * Cancels an existing SEPA payment.
     *
     * @param request The cancellation request
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(SepaCancellationRequestDTO request);

    /**
     * Schedules a SEPA payment for future execution.
     *
     * @param request The schedule request
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedule(SepaScheduleRequestDTO request);
}