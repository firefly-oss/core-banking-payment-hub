package com.firefly.core.banking.payments.hub.core.services;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Service interface for internal transfer operations.
 */
public interface InternalTransferService {

    /**
     * Simulates an internal transfer without actual execution.
     *
     * @param request The internal transfer request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateTransfer(InternalTransferRequestDTO request);

    /**
     * Executes an internal transfer.
     *
     * @param request The internal transfer request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executeTransfer(InternalTransferRequestDTO request);


    /**
     * Cancels an existing internal transfer using the cancellation request DTO.
     *
     * @param request The cancellation request containing transfer ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelTransfer(InternalTransferCancellationRequestDTO request);

    /**
     * Simulates the cancellation of an internal transfer.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(InternalTransferCancellationRequestDTO request);

    /**
     * Schedules an internal transfer for future execution.
     *
     * @param request The internal transfer request to schedule
     * @param executionDate The date when the transfer should be executed
     * @param recurrencePattern Optional recurrence pattern for recurring transfers
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> scheduleTransfer(InternalTransferRequestDTO request,
                                                  String executionDate,
                                                  String recurrencePattern);
}
