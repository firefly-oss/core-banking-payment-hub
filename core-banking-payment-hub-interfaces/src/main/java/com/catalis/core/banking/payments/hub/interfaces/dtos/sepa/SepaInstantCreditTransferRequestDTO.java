package com.catalis.core.banking.payments.hub.interfaces.dtos.sepa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SEPA Instant Credit Transfer (ICT) payment request.
 * Extends SepaCreditTransferRequestDTO with ICT-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SEPA Instant Credit Transfer (ICT) payment request")
public class SepaInstantCreditTransferRequestDTO extends SepaCreditTransferRequestDTO {
    
    @Schema(description = "Time limit for execution in seconds", example = "20")
    private Integer timeLimit;
    
    @Schema(description = "Flag indicating if the payment should be rejected if it cannot be executed instantly")
    private Boolean rejectIfNotInstant;
    
    @Schema(description = "Flag indicating if the payment should be executed as a regular SCT if instant is not possible")
    private Boolean fallbackToRegularSct;
    
    @Schema(description = "Notification URL for payment status updates", example = "https://example.com/payment-notifications")
    private String notificationUrl;
}
