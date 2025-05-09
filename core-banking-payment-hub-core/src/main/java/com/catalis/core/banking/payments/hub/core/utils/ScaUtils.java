package com.catalis.core.banking.payments.hub.core.utils;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for Strong Customer Authentication (SCA) related operations.
 * This class provides common functionality used across different payment providers.
 */
@Slf4j
public class ScaUtils {

    /**
     * Validates the provided SCA information.
     *
     * @param sca The SCA information to validate
     * @return The validation result
     */
    public static ScaResultDTO validateSca(ScaDTO sca) {
        // In a real implementation, this would validate the SCA against a backend system
        // For simulation, we'll accept a specific code or generate random success/failure

        ScaResultDTO result = new ScaResultDTO();
        result.setMethod(sca.getMethod());
        result.setChallengeId(sca.getChallengeId() != null ? sca.getChallengeId() : "CHL-" + UUID.randomUUID().toString().substring(0, 8));
        result.setVerificationTimestamp(LocalDateTime.now());
        result.setAttemptCount(1);
        result.setMaxAttempts(3);
        result.setExpired(false);
        result.setExpiryTimestamp(LocalDateTime.now().plusMinutes(15));

        // For testing, accept "123456" as a valid code
        if (sca.getAuthenticationCode() != null && "123456".equals(sca.getAuthenticationCode())) {
            result.setSuccess(true);
            result.setMessage("SCA validation successful");
        } else if (sca.getAuthenticationCode() == null) {
            result.setSuccess(false);
            result.setErrorCode("SCA_CODE_MISSING");
            result.setErrorMessage("Authentication code is required");
        } else {
            // Random success/failure for other codes
            boolean success = Math.random() > 0.3; // 70% success rate
            result.setSuccess(success);
            if (success) {
                result.setMessage("SCA validation successful");
            } else {
                result.setErrorCode("SCA_INVALID_CODE");
                result.setErrorMessage("Invalid authentication code");
            }
        }

        return result;
    }

    /**
     * Masks a phone number for privacy, showing only the last 4 digits.
     *
     * @param phoneNumber The phone number to mask
     * @return The masked phone number
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return phoneNumber;
        }
        return "*****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Generates a new simulation reference.
     *
     * @return A unique simulation reference
     */
    public static String generateSimulationReference() {
        return "SIM-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a new challenge ID for SCA.
     *
     * @return A unique challenge ID
     */
    public static String generateChallengeId() {
        return "CHL-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Creates a default SCA result with challenge information.
     *
     * @param method The SCA method
     * @param expiryTimestamp The expiry timestamp for the SCA code
     * @return A new SCA result
     */
    public static ScaResultDTO createDefaultScaResult(String method, LocalDateTime expiryTimestamp) {
        ScaResultDTO scaResult = new ScaResultDTO();
        scaResult.setMethod(method);
        scaResult.setChallengeId(generateChallengeId());
        scaResult.setVerificationTimestamp(null); // Not verified yet
        scaResult.setAttemptCount(0);
        scaResult.setMaxAttempts(3);
        scaResult.setExpired(false);
        scaResult.setExpiryTimestamp(expiryTimestamp);
        scaResult.setSuccess(false); // Not verified yet
        return scaResult;
    }
}
