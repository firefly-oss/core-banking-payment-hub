package com.firefly.core.banking.payments.hub.core.utils;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for Strong Customer Authentication (SCA) related operations.
 * This class provides common functionality used across different payment providers,
 * including support for biometric authentication methods.
 */
@Slf4j
public class ScaUtils {

    // List of supported SCA methods
    public static final String METHOD_SMS = "SMS";
    public static final String METHOD_EMAIL = "EMAIL";
    public static final String METHOD_APP = "APP";

    // Biometric authentication methods
    public static final String METHOD_BIOMETRIC_FINGERPRINT = "BIOMETRIC_FINGERPRINT";
    public static final String METHOD_BIOMETRIC_FACE = "BIOMETRIC_FACE";
    public static final String METHOD_BIOMETRIC_VOICE = "BIOMETRIC_VOICE";
    public static final String METHOD_BIOMETRIC_IRIS = "BIOMETRIC_IRIS";
    public static final String METHOD_BIOMETRIC_RETINA = "BIOMETRIC_RETINA";
    public static final String METHOD_BIOMETRIC_PALM = "BIOMETRIC_PALM";
    public static final String METHOD_BIOMETRIC_VEIN = "BIOMETRIC_VEIN";
    public static final String METHOD_BIOMETRIC_BEHAVIORAL = "BIOMETRIC_BEHAVIORAL";

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

        // Biometric methods typically have different parameters
        boolean isBiometric = isBiometricMethod(method);
        if (isBiometric) {
            scaResult.setMaxAttempts(1); // Biometric usually only allows one attempt
            scaResult.setAuthenticationMethod(method);
        } else {
            scaResult.setMaxAttempts(3);
        }

        scaResult.setExpired(false);
        scaResult.setExpiryTimestamp(expiryTimestamp);
        scaResult.setSuccess(false); // Not verified yet
        return scaResult;
    }

    /**
     * Checks if the specified method is a biometric authentication method.
     *
     * @param method The authentication method to check
     * @return true if the method is a biometric method, false otherwise
     */
    public static boolean isBiometricMethod(String method) {
        if (method == null) {
            return false;
        }
        return method.startsWith("BIOMETRIC_");
    }

    /**
     * Gets a user-friendly name for an SCA method.
     *
     * @param method The SCA method code
     * @return A user-friendly name for the method
     */
    public static String getMethodDisplayName(String method) {
        if (method == null) {
            return "Unknown";
        }

        switch (method) {
            case METHOD_SMS:
                return "SMS";
            case METHOD_EMAIL:
                return "Email";
            case METHOD_APP:
                return "Mobile App";
            case METHOD_BIOMETRIC_FINGERPRINT:
                return "Fingerprint";
            case METHOD_BIOMETRIC_FACE:
                return "Facial Recognition";
            case METHOD_BIOMETRIC_VOICE:
                return "Voice Recognition";
            case METHOD_BIOMETRIC_IRIS:
                return "Iris Scan";
            case METHOD_BIOMETRIC_RETINA:
                return "Retina Scan";
            case METHOD_BIOMETRIC_PALM:
                return "Palm Print";
            case METHOD_BIOMETRIC_VEIN:
                return "Vein Pattern";
            case METHOD_BIOMETRIC_BEHAVIORAL:
                return "Behavioral Biometrics";
            default:
                return method;
        }
    }
}
