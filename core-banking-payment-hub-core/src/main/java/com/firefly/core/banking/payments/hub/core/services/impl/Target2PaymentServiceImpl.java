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
import com.firefly.core.banking.payments.hub.core.services.Target2PaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.Target2CancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.Target2PaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.Target2PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of the Target2PaymentService interface.
 * Provides routing logic to delegate operations to the appropriate provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class Target2PaymentServiceImpl implements Target2PaymentService {

    private static final Logger log = LoggerFactory.getLogger(Target2PaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public Target2PaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized Target2PaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(Target2PaymentRequestDTO request) {
        log.debug("Simulating TARGET2 payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("TARGET2 payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating TARGET2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TARGET2 payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(Target2PaymentRequestDTO request) {
        log.debug("Executing TARGET2 payment: {}", request);
        return getProvider()
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("TARGET2 payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing TARGET2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TARGET2 payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(Target2CancellationRequestDTO request) {
        log.debug("Cancelling TARGET2 payment with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("TARGET2 payment cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling TARGET2 payment with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TARGET2 payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(Target2CancellationRequestDTO request) {
        log.debug("Simulating cancellation of TARGET2 payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("TARGET2 payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of TARGET2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TARGET2 payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(Target2PaymentRequestDTO request, String executionDate) {
        log.debug("Scheduling TARGET2 payment: {}, execution date: {}", request, executionDate);
        return getProvider()
                .map(provider -> provider.schedule(request, executionDate)
                    .doOnSuccess(result -> log.info("TARGET2 payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling TARGET2 payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TARGET2 payment provider available")));
    }

    /**
     * Gets the appropriate TARGET2 payment provider.
     *
     * @return The selected payment provider
     */
    private Optional<Target2PaymentProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.TARGET2_PROVIDER);
    }
}
