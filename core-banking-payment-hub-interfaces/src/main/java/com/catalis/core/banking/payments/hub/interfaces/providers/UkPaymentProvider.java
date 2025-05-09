package com.catalis.core.banking.payments.hub.interfaces.providers;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkBacsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkChapsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkFasterPaymentRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for UK payment operations.
 * Implementations of this interface will handle UK payment processing
 * for different payment types (FPS, BACS, CHAPS).
 */
public interface UkPaymentProvider {

    /**
     * Simulates a UK Faster Payment without actual execution.
     *
     * @param request The UK Faster Payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateFasterPayment(UkFasterPaymentRequestDTO request);

    /**
     * Executes a UK Faster Payment.
     *
     * @param request The UK Faster Payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executeFasterPayment(UkFasterPaymentRequestDTO request);


    /**
     * Cancels an existing UK Faster Payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelFasterPayment(UkCancellationRequestDTO request);

    /**
     * Simulates the cancellation of a UK Faster Payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateFasterPaymentCancellation(UkCancellationRequestDTO request);

    /**
     * Schedules a UK Faster Payment for future execution.
     *
     * @param request The UK Faster Payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> scheduleFasterPayment(UkFasterPaymentRequestDTO request, String executionDate);

    /**
     * Simulates a UK BACS payment without actual execution.
     *
     * @param request The UK BACS payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateBacsPayment(UkBacsPaymentRequestDTO request);

    /**
     * Executes a UK BACS payment.
     *
     * @param request The UK BACS payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executeBacsPayment(UkBacsPaymentRequestDTO request);


    /**
     * Cancels an existing UK BACS payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelBacsPayment(UkCancellationRequestDTO request);

    /**
     * Simulates the cancellation of a UK BACS payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateBacsPaymentCancellation(UkCancellationRequestDTO request);

    /**
     * Schedules a UK BACS payment for future execution.
     *
     * @param request The UK BACS payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> scheduleBacsPayment(UkBacsPaymentRequestDTO request, String executionDate);

    /**
     * Simulates a UK CHAPS payment without actual execution.
     *
     * @param request The UK CHAPS payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateChapsPayment(UkChapsPaymentRequestDTO request);

    /**
     * Executes a UK CHAPS payment.
     *
     * @param request The UK CHAPS payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executeChapsPayment(UkChapsPaymentRequestDTO request);


    /**
     * Cancels an existing UK CHAPS payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelChapsPayment(UkCancellationRequestDTO request);

    /**
     * Simulates the cancellation of a UK CHAPS payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateChapsPaymentCancellation(UkCancellationRequestDTO request);

    /**
     * Schedules a UK CHAPS payment for future execution.
     *
     * @param request The UK CHAPS payment request to schedule
     * @param executionDate The date when the payment should be executed
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> scheduleChapsPayment(UkChapsPaymentRequestDTO request, String executionDate);
}
