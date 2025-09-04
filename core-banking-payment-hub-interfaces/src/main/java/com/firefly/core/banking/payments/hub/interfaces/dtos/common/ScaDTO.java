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


package com.firefly.core.banking.payments.hub.interfaces.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Strong Customer Authentication (SCA) information.
 * Used for implementing PSD2 compliant authentication for payment operations.
 * Supports multiple authentication methods including biometric authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strong Customer Authentication information")
public class ScaDTO {

    /**
     * The SCA method to use for authentication.
     * Supported values:
     * - SMS: SMS-based authentication
     * - EMAIL: Email-based authentication
     * - APP: Mobile app-based authentication
     * - BIOMETRIC_FINGERPRINT: Fingerprint authentication
     * - BIOMETRIC_FACE: Facial recognition authentication
     * - BIOMETRIC_VOICE: Voice recognition authentication
     */
    @Schema(description = "SCA method (SMS, EMAIL, APP, BIOMETRIC_FINGERPRINT, BIOMETRIC_FACE, BIOMETRIC_VOICE)", example = "SMS")
    private String method;

    @Schema(description = "Recipient identifier (phone number, email, device ID)", example = "+34600000000")
    private String recipient;

    @Schema(description = "Authentication code provided by the user or biometric verification token", example = "123456")
    private String authenticationCode;

    @Schema(description = "Challenge ID for the SCA process", example = "CHL-123456789")
    private String challengeId;

    @Schema(description = "Flag indicating if SCA is required for this operation")
    private Boolean required;

    @Schema(description = "Flag indicating if SCA has been completed")
    private Boolean completed;

    @Schema(description = "Timestamp when the SCA challenge was created")
    private String challengeTimestamp;

    @Schema(description = "Timestamp when the SCA challenge expires")
    private String expiryTimestamp;

    /**
     * Additional data for biometric authentication.
     * This could include device information, biometric verification tokens,
     * or other data needed for biometric authentication.
     */
    @Schema(description = "Additional data for biometric authentication")
    private String biometricData;

    /**
     * The device ID used for biometric authentication.
     * This is required when using biometric authentication methods.
     */
    @Schema(description = "Device ID for biometric authentication", example = "device-123456")
    private String deviceId;
}
