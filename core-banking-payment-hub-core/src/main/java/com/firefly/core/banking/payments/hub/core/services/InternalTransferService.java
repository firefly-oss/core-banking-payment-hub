/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
