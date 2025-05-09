package com.catalis.core.banking.payments.hub.interfaces.dtos.sepa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SEPA Instant Credit Transfer (SCT Inst) payment request.
 * Extends SepaCreditTransferRequestDTO with SCT Inst-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SEPA Instant Credit Transfer (SCT Inst) payment request")
public class SepaInstantCreditTransferRequestDTO extends SepaCreditTransferRequestDTO {

    @Schema(description = "Time limit for execution in seconds", example = "20")
    private Integer timeLimit;

    @Schema(description = "Flag indicating if the payment should be rejected if it cannot be executed instantly")
    private Boolean rejectIfNotInstant;

    @Schema(description = "Flag indicating if the payment should be executed as a regular SCT if instant is not possible")
    private Boolean fallbackToRegularSct;

    @Schema(description = "Notification URL for payment status updates", example = "https://example.com/payment-notifications")
    private String notificationUrl;

    @Schema(description = "Maximum execution time in seconds", example = "20")
    private Integer maxExecutionTimeSeconds;

    @Schema(description = "Beneficiary notification required", example = "true")
    private Boolean beneficiaryNotificationRequired;

    @Schema(description = "Beneficiary notification method (SMS, EMAIL, PUSH)", example = "SMS")
    private String beneficiaryNotificationMethod;

    @Schema(description = "Beneficiary notification contact", example = "+34600000000")
    private String beneficiaryNotificationContact;

    @Schema(description = "Originator notification required", example = "true")
    private Boolean originatorNotificationRequired;

    @Schema(description = "Originator notification method (SMS, EMAIL, PUSH)", example = "EMAIL")
    private String originatorNotificationMethod;

    @Schema(description = "Originator notification contact", example = "originator@example.com")
    private String originatorNotificationContact;
}
