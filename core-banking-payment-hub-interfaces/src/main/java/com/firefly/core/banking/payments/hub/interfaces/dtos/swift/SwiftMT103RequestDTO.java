/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
