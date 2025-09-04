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
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.Target2CancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.Target2PaymentRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Service interface for TARGET2 payment operations.
 */
public interface Target2PaymentService {

    /**
     * Simulates a TARGET2 payment without actual execution.
     *
     * @param request The TARGET2 payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulatePayment(Target2PaymentRequestDTO request);

    /**
     * Executes a TARGET2 payment.
     *
     * @param request The TARGET2 payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> executePayment(Target2PaymentRequestDTO request);


    /**
     * Cancels an existing TARGET2 payment using the cancellation request DTO.
     *
     * @param request The cancellation request containing payment ID, reason, and SCA information
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancelPayment(Target2CancellationRequestDTO request);

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
     * @param executionDate The date when the payment should be executed (YYYY-MM-DD)
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedulePayment(Target2PaymentRequestDTO request, String executionDate);
}
