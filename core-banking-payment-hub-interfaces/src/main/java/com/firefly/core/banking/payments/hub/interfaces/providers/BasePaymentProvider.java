package com.firefly.core.banking.payments.hub.interfaces.providers;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import reactor.core.publisher.Mono;

/**
 * Base interface for all payment providers.
 * Defines common SCA-related operations that all payment providers should support.
 */
public interface BasePaymentProvider {

    /**
     * Triggers SCA for a payment operation.
     * This method initiates the SCA process by sending a challenge to the user.
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
     * Checks if the provider is healthy and operational.
     * This method is used by health checks to determine the provider's status.
     *
     * @return A Mono emitting a boolean indicating if the provider is healthy
     */
    Mono<Boolean> isHealthy();
}
