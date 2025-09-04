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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Strong Customer Authentication (SCA) verification results.
 * Used to return the status and details of an SCA verification process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strong Customer Authentication verification result")
public class ScaResultDTO {

    @Schema(description = "Flag indicating if SCA verification was successful", required = true)
    private boolean success;

    @Schema(description = "SCA method used (SMS, EMAIL, APP, BIOMETRIC)", example = "SMS")
    private String method;

    @Schema(description = "Challenge ID for the SCA process", example = "CHL-123456789")
    private String challengeId;

    @Schema(description = "Timestamp when the SCA verification was completed")
    private LocalDateTime verificationTimestamp;

    @Schema(description = "Error code in case of verification failure")
    private String errorCode;

    @Schema(description = "Error message in case of verification failure")
    private String errorMessage;

    @Schema(description = "Number of attempts made for this verification")
    private Integer attemptCount;

    @Schema(description = "Maximum number of attempts allowed")
    private Integer maxAttempts;

    @Schema(description = "Flag indicating if the verification has expired")
    private boolean expired;

    @Schema(description = "Timestamp when the verification expires")
    private LocalDateTime expiryTimestamp;

    // Additional fields for compatibility with provider implementations
    @Schema(description = "Authentication message")
    private String message;

    @Schema(description = "Authentication method used")
    private String authenticationMethod;

    @Schema(description = "Timestamp when the authentication was completed")
    private LocalDateTime authenticationTimestamp;

    /**
     * Gets the message for this SCA result.
     *
     * @return the message
     */
    public String getMessage() {
        return message != null ? message : errorMessage;
    }

    /**
     * Sets the message for this SCA result.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the authentication method for this SCA result.
     *
     * @param authenticationMethod the authentication method to set
     */
    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
        this.method = authenticationMethod; // For backward compatibility
    }

    /**
     * Sets the authentication timestamp for this SCA result.
     *
     * @param authenticationTimestamp the authentication timestamp to set
     */
    public void setAuthenticationTimestamp(LocalDateTime authenticationTimestamp) {
        this.authenticationTimestamp = authenticationTimestamp;
        this.verificationTimestamp = authenticationTimestamp; // For backward compatibility
    }
}
