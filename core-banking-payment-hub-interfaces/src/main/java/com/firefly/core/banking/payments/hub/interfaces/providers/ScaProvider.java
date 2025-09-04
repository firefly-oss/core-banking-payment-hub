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

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import reactor.core.publisher.Mono;

/**
 * Provider interface for Strong Customer Authentication (SCA) operations.
 * This interface defines methods for triggering and validating SCA
 * that can be used by different payment providers.
 */
public interface ScaProvider {

    /**
     * Triggers SCA for a payment operation.
     * This method initiates the SCA process by sending a challenge to the user
     * via the specified method (SMS, email, etc.).
     *
     * @param recipientIdentifier The recipient identifier (phone number, email, etc.)
     * @param method The SCA method (SMS, EMAIL, APP, BIOMETRIC)
     * @param referenceId A reference ID for the operation requiring SCA
     * @return A Mono emitting the SCA challenge information
     */
    Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId);

    /**
     * Validates an SCA challenge response.
     * This method verifies the authentication code provided by the user.
     *
     * @param sca The SCA information including the authentication code
     * @return A Mono emitting the validation result
     */
    Mono<ScaResultDTO> validateSca(ScaDTO sca);
    
    /**
     * Checks if SCA is required for a specific operation.
     * This method determines whether SCA should be applied based on
     * operation type, amount, and other risk factors.
     *
     * @param operationType The type of operation (PAYMENT, CANCELLATION, etc.)
     * @param amount The amount of the operation
     * @param currency The currency of the operation
     * @param accountId The account ID involved in the operation
     * @return A Mono emitting a boolean indicating if SCA is required
     */
    Mono<Boolean> isScaRequired(String operationType, String amount, String currency, String accountId);
}
