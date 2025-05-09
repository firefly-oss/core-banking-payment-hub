package com.catalis.core.banking.payments.hub.interfaces.dtos.sepa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * SEPA Direct Debit Business-to-Business (SDD B2B) payment request.
 * Extends SepaPaymentRequestDTO with SDD B2B-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SEPA Direct Debit Business-to-Business (SDD B2B) payment request")
public class SepaDirectDebitB2BRequestDTO extends SepaPaymentRequestDTO {

    @NotBlank(message = "Mandate ID is required")
    @Size(max = 35, message = "Mandate ID must not exceed 35 characters")
    @Schema(description = "Mandate ID", example = "B2B-MANDATE-12345", required = true)
    private String mandateId;

    @NotBlank(message = "Mandate signature date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Mandate signature date must be in format YYYY-MM-DD")
    @Schema(description = "Mandate signature date", example = "2023-01-15", required = true)
    private String mandateSignatureDate;

    @NotBlank(message = "Creditor ID is required")
    @Schema(description = "Creditor ID", example = "DE98ZZZ09999999999", required = true)
    private String creditorId;

    @Schema(description = "Sequence type (FRST, RCUR, FNAL, OOFF)", example = "FRST")
    private String sequenceType;

    @Schema(description = "Amendment indicator", example = "false")
    private Boolean amendmentIndicator;

    @Schema(description = "Original mandate ID", example = "B2B-MANDATE-12345-OLD")
    private String originalMandateId;

    @Schema(description = "Original creditor ID", example = "DE98ZZZ09999999999-OLD")
    private String originalCreditorId;

    @Schema(description = "Original debtor account IBAN", example = "DE89370400440532013000")
    private String originalDebtorAccountIban;

    @Schema(description = "Pre-notification identifier", example = "PRENOT-12345")
    private String preNotificationId;

    @Schema(description = "Pre-notification date", example = "2023-01-10")
    private String preNotificationDate;
    
    @Schema(description = "Business identifier code", example = "BIC-12345")
    private String businessIdentifierCode;
    
    @Schema(description = "Contract reference", example = "CONTRACT-12345")
    private String contractReference;
}
