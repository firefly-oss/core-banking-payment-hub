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
import com.firefly.core.banking.payments.hub.core.services.TipsPaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.TipsCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.TipsPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.TipsPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of the TipsPaymentService interface.
 * Provides routing logic to delegate operations to the appropriate provider.
 * Uses the PaymentProviderRegistry to dynamically discover and use available providers.
 */
@Service
public class TipsPaymentServiceImpl implements TipsPaymentService {

    private static final Logger log = LoggerFactory.getLogger(TipsPaymentServiceImpl.class);

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public TipsPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
        log.info("Initialized TipsPaymentServiceImpl with provider registry");
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(TipsPaymentRequestDTO request) {
        log.debug("Simulating TIPS payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulate(request)
                    .doOnSuccess(result -> log.info("TIPS payment simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating TIPS payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TIPS payment provider available")));
    }

    @Override
    public Mono<PaymentExecutionResultDTO> executePayment(TipsPaymentRequestDTO request) {
        log.debug("Executing TIPS payment: {}", request);
        return getProvider()
                .map(provider -> provider.execute(request)
                    .doOnSuccess(result -> log.info("TIPS payment execution completed: {}", result))
                    .doOnError(error -> log.error("Error executing TIPS payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TIPS payment provider available")));
    }



    @Override
    public Mono<PaymentCancellationResultDTO> cancelPayment(TipsCancellationRequestDTO request) {
        log.debug("Cancelling TIPS payment with SCA: {}", request);
        return getProvider()
                .map(provider -> provider.cancel(request)
                    .doOnSuccess(result -> log.info("TIPS payment cancellation with SCA completed: {}", result))
                    .doOnError(error -> log.error("Error cancelling TIPS payment with SCA", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TIPS payment provider available")));
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulateCancellation(TipsCancellationRequestDTO request) {
        log.debug("Simulating cancellation of TIPS payment: {}", request);
        return getProvider()
                .map(provider -> provider.simulateCancellation(request)
                    .doOnSuccess(result -> log.info("TIPS payment cancellation simulation completed: {}", result))
                    .doOnError(error -> log.error("Error simulating cancellation of TIPS payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TIPS payment provider available")));
    }

    @Override
    public Mono<PaymentScheduleResultDTO> schedulePayment(TipsPaymentRequestDTO request, String executionDate) {
        log.debug("Scheduling TIPS payment: {}, execution date: {}", request, executionDate);
        return getProvider()
                .map(provider -> provider.schedule(request, executionDate)
                    .doOnSuccess(result -> log.info("TIPS payment scheduling completed: {}", result))
                    .doOnError(error -> log.error("Error scheduling TIPS payment", error)))
                .orElseGet(() -> Mono.error(new IllegalStateException("No TIPS payment provider available")));
    }

    /**
     * Gets the appropriate TIPS payment provider.
     *
     * @return The selected payment provider
     */
    private Optional<TipsPaymentProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.TIPS_PROVIDER);
    }
}
