package com.firefly.core.banking.payments.hub.interfaces.dtos.swift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SWIFT MT103 (Customer Credit Transfer) payment request.
 * Extends SwiftPaymentRequestDTO with MT103-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SWIFT MT103 (Customer Credit Transfer) payment request")
public class SwiftMT103RequestDTO extends SwiftPaymentRequestDTO {
    
    @Schema(description = "Remittance information", example = "Invoice payment #12345")
    private String remittanceInformation;
    
    @Schema(description = "Cheque number", example = "CHK-123456789")
    private String chequeNumber;
    
    @Schema(description = "Bank operation code", example = "CRED")
    private String bankOperationCode;
    
    @Schema(description = "Instruction code for account with institution", example = "PHON")
    private String instructionCodeForAccountWithInstitution;
    
    @Schema(description = "Sender to receiver information", example = "Please notify beneficiary upon receipt")
    private String senderToReceiverInformation;
    
    @Schema(description = "Regulatory reporting details")
    private String regulatoryReportingDetails;
    
    @Schema(description = "Details of charges", example = "OUR")
    private String detailsOfCharges;
    
    @Schema(description = "Exchange rate", example = "1.1050")
    private String exchangeRate;
}
