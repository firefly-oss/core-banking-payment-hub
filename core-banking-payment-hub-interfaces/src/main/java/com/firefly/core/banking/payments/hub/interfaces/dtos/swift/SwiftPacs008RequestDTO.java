package com.firefly.core.banking.payments.hub.interfaces.dtos.swift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SWIFT PACS.008 (MX Customer Credit Transfer) payment request.
 * Extends SwiftPaymentRequestDTO with PACS.008-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SWIFT PACS.008 (MX Customer Credit Transfer) payment request")
public class SwiftPacs008RequestDTO extends SwiftPaymentRequestDTO {
    
    @Schema(description = "Group header ID", example = "GH-123456789")
    private String groupHeaderId;
    
    @Schema(description = "Settlement method", example = "CLRG")
    private String settlementMethod;
    
    @Schema(description = "Clearing system", example = "CHIPS")
    private String clearingSystem;
    
    @Schema(description = "Service level", example = "SEPA")
    private String serviceLevel;
    
    @Schema(description = "Local instrument", example = "INST")
    private String localInstrument;
    
    @Schema(description = "Category purpose", example = "CASH")
    private String categoryPurpose;
    
    @Schema(description = "Instruction priority", example = "HIGH")
    private String instructionPriority;
    
    @Schema(description = "Ultimate debtor name", example = "Ultimate Debtor Ltd")
    private String ultimateDebtor;
    
    @Schema(description = "Ultimate creditor name", example = "Ultimate Creditor Ltd")
    private String ultimateCreditor;
    
    @Schema(description = "Structured remittance information")
    private StructuredRemittanceInfoDTO structuredRemittanceInfo;
    
    /**
     * Structured remittance information for SWIFT PACS.008.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Structured remittance information")
    public static class StructuredRemittanceInfoDTO {
        
        @Schema(description = "Reference type", example = "SCOR")
        private String referenceType;
        
        @Schema(description = "Reference", example = "RF18539007547034")
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
