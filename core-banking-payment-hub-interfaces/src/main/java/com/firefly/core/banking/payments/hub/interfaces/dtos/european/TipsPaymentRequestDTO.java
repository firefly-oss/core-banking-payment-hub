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


package com.firefly.core.banking.payments.hub.interfaces.dtos.european;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.AccountInfoDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.BasePaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PartyInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * TIPS (TARGET Instant Payment Settlement) payment request.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "TIPS (TARGET Instant Payment Settlement) payment request")
public class TipsPaymentRequestDTO extends BasePaymentRequestDTO {

    @Valid
    @NotNull(message = "Debtor information is required")
    @Schema(description = "Debtor (payer) information", required = true)
    private PartyInfoDTO debtor;

    @Valid
    @NotNull(message = "Debtor account information is required")
    @Schema(description = "Debtor (payer) account information", required = true)
    private AccountInfoDTO debtorAccount;

    @Valid
    @NotNull(message = "Creditor information is required")
    @Schema(description = "Creditor (payee) information", required = true)
    private PartyInfoDTO creditor;

    @Valid
    @NotNull(message = "Creditor account information is required")
    @Schema(description = "Creditor (payee) account information", required = true)
    private AccountInfoDTO creditorAccount;

    @NotBlank(message = "Remittance information is required")
    @Size(max = 140, message = "Remittance information must not exceed 140 characters")
    @Schema(description = "Remittance information / payment details", example = "Invoice 12345", required = true)
    private String remittanceInformation;

    @Schema(description = "End-to-end identification", example = "E2E-12345")
    private String endToEndId;

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

    @Schema(description = "TIPS message type (pacs.008, pacs.002, etc.)", example = "pacs.008")
    private String messageType;

    @Schema(description = "TIPS service level", example = "SEPA")
    private String serviceLevel;

    @Schema(description = "TIPS clearing system", example = "TIPS")
    private String clearingSystem;

    @Schema(description = "TIPS member identification", example = "DEUTDEFFXXX")
    private String memberIdentification;
}
