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


package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.firefly.core.banking.payments.hub.core.services.AchPaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.ach.AchCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.AchPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of the AchPaymentService interface.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class AchPaymentServiceImpl implements AchPaymentService {

    private static final Logger log = LoggerFactory.getLogger(AchPaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public AchPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized AchPaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(AchTransferRequestDTO request) {
        log.debug("Simulating ACH payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("ACH payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating ACH payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No ACH payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(AchTransferRequestDTO request) {
        log.debug("Executing ACH payment: {}", request);
        return getProvider()
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("ACH payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing ACH payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No ACH payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(AchCancellationRequestDTO request) {
        log.debug("Cancelling ACH payment: {}", request);
        return getProvider()
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("ACH payment cancellation completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling ACH payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No ACH payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(AchCancellationRequestDTO request) {
        log.debug("Simulating cancellation of ACH payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("ACH payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of ACH payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No ACH payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(AchTransferRequestDTO request,
                                                        String executionDate,
                                                        String recurrencePattern) {
        log.debug("Scheduling ACH payment: {}, execution date: {}, recurrence: {}",
                request, executionDate, recurrencePattern);
        return getProvider()
                .map(provider -> provider.schedule(request, executionDate, recurrencePattern)
                    .doOnSuccess(result -> log.info("ACH payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling ACH payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No ACH payment provider available")));
    }

    /**
     * Gets the ACH payment provider.
     *
     * @return The ACH payment provider
     */
    private Optional<AchPaymentProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.ACH_PROVIDER);
    }
}
