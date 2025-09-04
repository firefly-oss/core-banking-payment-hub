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
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardScheduleRequestDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for card payment operations.
 * Implementations of this interface will handle card payment processing
 * for different card processors (e.g., Visa, Mastercard, Amex).
 * 
 * This provider acts as the card authorization center interface, defining
 * the contract for all card payment operations following hexagonal architecture.
 */
public interface CardPaymentProvider extends BasePaymentProvider {

    /**
     * Simulates a card payment without actual execution.
     * This includes card validation, authorization checks, and risk assessment.
     *
     * @param request The card payment request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulate(CardPaymentRequestDTO request);

    /**
     * Executes a card payment.
     * This performs the actual card authorization and capture.
     *
     * @param request The card payment request to execute
     * @return A Mono emitting the execution result
     */
    Mono<PaymentExecutionResultDTO> execute(CardPaymentRequestDTO request);

    /**
     * Simulates the cancellation of a card payment.
     * This is used to trigger SCA delivery and provide information about the cancellation.
     *
     * @param request The cancellation request to simulate
     * @return A Mono emitting the simulation result
     */
    Mono<PaymentSimulationResultDTO> simulateCancellation(CardCancellationRequestDTO request);

    /**
     * Cancels an existing card payment.
     * This performs a void (if not settled) or refund (if settled) operation.
     *
     * @param request The cancellation request
     * @return A Mono emitting the cancellation result
     */
    Mono<PaymentCancellationResultDTO> cancel(CardCancellationRequestDTO request);

    /**
     * Schedules a card payment for future execution.
     * This may involve storing card tokens and setting up recurring authorizations.
     *
     * @param request The schedule request
     * @return A Mono emitting the scheduling result
     */
    Mono<PaymentScheduleResultDTO> schedule(CardScheduleRequestDTO request);
}