package com.catalis.core.banking.payments.hub.interfaces.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Strong Customer Authentication (SCA) information.
 * Used for implementing PSD2 compliant authentication for payment operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strong Customer Authentication information")
public class ScaDTO {
    
    @Schema(description = "SCA method (SMS, EMAIL, APP, BIOMETRIC)", example = "SMS")
    private String method;
    
    @Schema(description = "Recipient identifier (phone number, email, device ID)", example = "+34600000000")
    private String recipient;
    
    @Schema(description = "Authentication code provided by the user", example = "123456")
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
}
