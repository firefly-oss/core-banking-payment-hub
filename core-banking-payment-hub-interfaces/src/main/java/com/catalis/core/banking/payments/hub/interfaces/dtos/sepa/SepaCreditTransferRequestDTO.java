package com.catalis.core.banking.payments.hub.interfaces.dtos.sepa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SEPA Credit Transfer (SCT) payment request.
 * Extends SepaPaymentRequestDTO with SCT-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SEPA Credit Transfer (SCT) payment request")
public class SepaCreditTransferRequestDTO extends SepaPaymentRequestDTO {

    @Schema(description = "Ultimate debtor name", example = "Ultimate Debtor Ltd")
    private String ultimateDebtor;

    @Schema(description = "Ultimate creditor name", example = "Ultimate Creditor Ltd")
    private String ultimateCreditor;

    @Schema(description = "Purpose code (ISO 20022)", example = "CASH")
    private String purposeCode;

    @Schema(description = "Regulatory reporting information")
    private String regulatoryReporting;

    @Schema(description = "Instruction for creditor agent", example = "CHQB")
    private String instructionForCreditorAgent;

    @Schema(description = "Instruction for debtor agent", example = "PHOB")
    private String instructionForDebtorAgent;

    @Schema(description = "Structured remittance information")
    private StructuredRemittanceInfoDTO structuredRemittanceInfo;

    /**
     * Structured remittance information for SEPA Credit Transfer.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Structured remittance information")
    public static class StructuredRemittanceInfoDTO {

        @Schema(description = "Creditor reference type", example = "SCOR")
        private String referenceType;

        @Schema(description = "Creditor reference", example = "RF18539007547034")
        private String reference;

        @Schema(description = "Reference issuer", example = "Issuer Name")
        private String referenceIssuer;

        @Schema(description = "Document type", example = "CINV")
        private String documentType;

        @Schema(description = "Document number", example = "INV-12345")
        private String documentNumber;

        @Schema(description = "Document related date", example = "2023-10-15")
        private String documentRelatedDate;
    }
}
