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


package com.firefly.core.banking.payments.hub.interfaces.providers;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.TipsCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.TipsPaymentRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for TIPS (TARGET Instant Payment Settlement) payment operations.
 * Implementations of this interface will handle TIPS payment processing.
 */
public interface TipsPaymentProvider extends BasePaymentProvider {

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
