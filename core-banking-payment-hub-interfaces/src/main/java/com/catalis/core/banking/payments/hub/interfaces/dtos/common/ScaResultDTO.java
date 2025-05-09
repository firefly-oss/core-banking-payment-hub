package com.catalis.core.banking.payments.hub.interfaces.dtos.common;

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
}
