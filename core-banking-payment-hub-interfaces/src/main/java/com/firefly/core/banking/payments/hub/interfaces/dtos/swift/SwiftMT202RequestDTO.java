package com.firefly.core.banking.payments.hub.interfaces.dtos.swift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SWIFT MT202 (Financial Institution Transfer) payment request.
 * Extends SwiftPaymentRequestDTO with MT202-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SWIFT MT202 (Financial Institution Transfer) payment request")
public class SwiftMT202RequestDTO extends SwiftPaymentRequestDTO {
    
    @Schema(description = "Related reference", example = "REL-REF-12345")
    private String relatedReference;
    
    @Schema(description = "Time indication", example = "CLSTIME")
    private String timeIndication;
    
    @Schema(description = "Settlement priority", example = "URGP")
    private String settlementPriority;
    
    @Schema(description = "Settlement method", example = "COVE")
    private String settlementMethod;
    
    @Schema(description = "Clearing system", example = "CHIPS")
    private String clearingSystem;
    
    @Schema(description = "Sender to receiver information", example = "Please process with high priority")
    private String senderToReceiverInformation;
    
    @Schema(description = "Bank operation code", example = "CRED")
    private String bankOperationCode;
    
    @Schema(description = "Exchange rate", example = "1.1050")
    private String exchangeRate;
}
