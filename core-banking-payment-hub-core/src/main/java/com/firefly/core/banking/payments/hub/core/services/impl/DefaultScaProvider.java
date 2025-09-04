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

import com.firefly.core.banking.payments.hub.core.utils.MetricsUtils;
import com.firefly.core.banking.payments.hub.core.utils.ScaUtils;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.providers.ScaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Arrays;

/**
 * Default implementation of the ScaProvider interface.
 * Provides functionality for triggering and validating SCA,
 * including support for biometric authentication methods.
 */
@Slf4j
@Component
public class DefaultScaProvider implements ScaProvider {

    private static final BigDecimal SCA_THRESHOLD_AMOUNT = new BigDecimal("500.00");

    // List of supported biometric authentication methods
    private static final String[] BIOMETRIC_METHODS = {
        ScaUtils.METHOD_BIOMETRIC_FINGERPRINT,
        ScaUtils.METHOD_BIOMETRIC_FACE,
        ScaUtils.METHOD_BIOMETRIC_VOICE,
        ScaUtils.METHOD_BIOMETRIC_IRIS,
        ScaUtils.METHOD_BIOMETRIC_RETINA,
        ScaUtils.METHOD_BIOMETRIC_PALM,
        ScaUtils.METHOD_BIOMETRIC_VEIN,
        ScaUtils.METHOD_BIOMETRIC_BEHAVIORAL
    };

    @Override
    public Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId) {
        log.info("Triggering SCA for recipient: {}, method: {}, reference: {}",
                ScaUtils.maskPhoneNumber(recipientIdentifier), method, referenceId);

        Instant start = Instant.now();

        // Check if the method is supported
        if (method == null) {
            log.warn("SCA method not specified, defaulting to SMS");
            method = "SMS";
        }

        // Handle different authentication methods
        boolean isBiometric = isBiometricMethod(method);

        // In a real implementation, this would call an external service to deliver the SCA challenge
        // For simulation, we'll create a mock SCA result
        LocalDateTime expiryTimestamp = LocalDateTime.now().plusMinutes(15);
        ScaResultDTO scaResult = ScaUtils.createDefaultScaResult(method, expiryTimestamp);

        // Set method-specific information
        if (isBiometric) {
            scaResult.setMessage("Biometric authentication requested via " + method);
            scaResult.setAuthenticationMethod(method);
        } else {
            scaResult.setMessage("SCA challenge sent to " + ScaUtils.maskPhoneNumber(recipientIdentifier));

            // Log the simulated code for testing purposes (only for non-biometric methods)
            log.debug("Simulated SCA code for challenge {}: 123456", scaResult.getChallengeId());
        }

        // Record metrics
        Duration duration = Duration.between(start, Instant.now());
        MetricsUtils.recordScaMetrics("trigger", method, duration.toMillis(), true);

        return Mono.just(scaResult);
    }

    @Override
    public Mono<ScaResultDTO> validateSca(ScaDTO sca) {
        log.info("Validating SCA challenge: {}, method: {}", sca.getChallengeId(), sca.getMethod());

        Instant start = Instant.now();

        // Check if this is a biometric authentication
        boolean isBiometric = isBiometricMethod(sca.getMethod());

        ScaResultDTO result;
        if (isBiometric) {
            // For biometric authentication, validate using biometric-specific logic
            result = validateBiometricSca(sca);
        } else {
            // For traditional methods, use the existing validation logic
            result = ScaUtils.validateSca(sca);
        }

        log.info("SCA validation result for challenge {}: {}",
                sca.getChallengeId(), result.isSuccess() ? "SUCCESS" : "FAILED");

        // Record metrics
        Duration duration = Duration.between(start, Instant.now());
        MetricsUtils.recordScaMetrics("validate", sca.getMethod(), duration.toMillis(), result.isSuccess());

        return Mono.just(result);
    }

    @Override
    public Mono<Boolean> isScaRequired(String operationType, String amount, String currency, String accountId) {
        log.debug("Checking if SCA is required for operation: {}, amount: {}, currency: {}, account: {}",
                operationType, amount, currency, accountId);

        Instant start = Instant.now();
        boolean scaRequired = false;

        // Check if amount exceeds threshold
        try {
            BigDecimal operationAmount = new BigDecimal(amount);
            if (operationAmount.compareTo(SCA_THRESHOLD_AMOUNT) > 0) {
                scaRequired = true;
                log.debug("SCA required due to amount exceeding threshold");
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid amount format: {}", amount);
        }

        // Check if operation type requires SCA
        if ("CANCEL".equals(operationType)) {
            // For cancellations, we might want to require SCA based on different criteria
            // For example, only require SCA for cancellations of high-value payments
            // or payments that are close to their execution date
            log.debug("Evaluating SCA requirement for cancellation operation");
        }

        // Additional risk-based checks could be implemented here
        // For example, checking if the operation is unusual for the account

        log.info("SCA requirement decision for operation {}: {}", operationType, scaRequired ? "REQUIRED" : "NOT REQUIRED");

        // Record metrics
        Duration duration = Duration.between(start, Instant.now());
        MetricsUtils.recordScaMetrics("requirement_check", operationType, duration.toMillis(), true);

        return Mono.just(scaRequired);
    }

    /**
     * Checks if the specified method is a biometric authentication method.
     *
     * @param method The authentication method to check
     * @return true if the method is a biometric method, false otherwise
     */
    private boolean isBiometricMethod(String method) {
        if (method == null) {
            return false;
        }
        return Arrays.asList(BIOMETRIC_METHODS).contains(method);
    }

    /**
     * Validates biometric authentication.
     * In a real implementation, this would verify the biometric data with a biometric service.
     *
     * @param sca The SCA information containing biometric data
     * @return The validation result
     */
    private ScaResultDTO validateBiometricSca(ScaDTO sca) {
        log.debug("Validating biometric authentication: method={}, deviceId={}", sca.getMethod(), sca.getDeviceId());

        ScaResultDTO result = new ScaResultDTO();
        result.setMethod(sca.getMethod());
        result.setChallengeId(sca.getChallengeId() != null ? sca.getChallengeId() : "CHL-" + UUID.randomUUID().toString().substring(0, 8));
        result.setVerificationTimestamp(LocalDateTime.now());
        result.setAttemptCount(1);
        result.setMaxAttempts(1); // Biometric usually only allows one attempt
        result.setExpired(false);
        result.setExpiryTimestamp(LocalDateTime.now().plusMinutes(5)); // Shorter expiry for biometric

        // For simulation, we'll accept biometric authentication with a valid device ID
        if (sca.getDeviceId() != null && !sca.getDeviceId().isEmpty()) {
            // In a real implementation, we would validate the biometric data against a service
            // Different validation logic based on the biometric method
            switch (sca.getMethod()) {
                case ScaUtils.METHOD_BIOMETRIC_FINGERPRINT:
                    result.setSuccess(true);
                    result.setMessage("Fingerprint authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_FACE:
                    result.setSuccess(true);
                    result.setMessage("Facial recognition authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_VOICE:
                    result.setSuccess(true);
                    result.setMessage("Voice recognition authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_IRIS:
                    result.setSuccess(true);
                    result.setMessage("Iris scan authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_RETINA:
                    result.setSuccess(true);
                    result.setMessage("Retina scan authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_PALM:
                    result.setSuccess(true);
                    result.setMessage("Palm print authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_VEIN:
                    result.setSuccess(true);
                    result.setMessage("Vein pattern authentication successful");
                    break;
                case ScaUtils.METHOD_BIOMETRIC_BEHAVIORAL:
                    result.setSuccess(true);
                    result.setMessage("Behavioral biometrics authentication successful");
                    break;
                default:
                    result.setSuccess(true);
                    result.setMessage("Biometric authentication successful");
                    break;
            }
            result.setAuthenticationMethod(sca.getMethod());
            result.setAuthenticationTimestamp(LocalDateTime.now());
        } else {
            result.setSuccess(false);
            result.setErrorCode("BIOMETRIC_DEVICE_MISSING");
            result.setErrorMessage("Device ID is required for biometric authentication");
        }

        return result;
    }
}
