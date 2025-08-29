package com.firefly.core.banking.payments.hub.interfaces.dtos.sepa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * SEPA Direct Debit (SDD) payment request.
 * Extends SepaPaymentRequestDTO with SDD-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SEPA Direct Debit (SDD) payment request")
public class SepaDirectDebitRequestDTO extends SepaPaymentRequestDTO {
    
    @NotBlank(message = "Mandate ID is required")
    @Schema(description = "Mandate identifier", example = "MND-123456789", required = true)
    private String mandateId;
    
    @NotNull(message = "Mandate signature date is required")
    @Schema(description = "Date when the mandate was signed", example = "2023-01-15", required = true)
    private LocalDate mandateSignatureDate;
    
    @Schema(description = "Sequence type (FRST, RCUR, FNAL, OOFF)", example = "FRST")
    private String sequenceType;
    
    @Schema(description = "Creditor scheme ID", example = "DE98ZZZ09999999999")
    private String creditorSchemeId;
    
    @Schema(description = "Pre-notification date", example = "2023-12-15")
    private LocalDate preNotificationDate;
    
    @Schema(description = "Flag indicating if the mandate is electronic")
    private Boolean electronicMandate;
    
    @Schema(description = "Mandate amendment information indicator")
    private Boolean amendmentIndicator;
    
    @Schema(description = "Original mandate ID (if amended)", example = "MND-987654321")
    private String originalMandateId;
    
    @Schema(description = "Original creditor scheme ID (if amended)", example = "DE98ZZZ09999999988")
    private String originalCreditorSchemeId;
    
    @Schema(description = "Original creditor name (if amended)", example = "Original Creditor Ltd")
    private String originalCreditorName;
    
    @Schema(description = "Original debtor account IBAN (if amended)", example = "DE89370400440532013000")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Original debtor IBAN must be in valid format")
    private String originalDebtorAccountIban;
}
