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
 * TARGET2 payment request.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "TARGET2 payment request")
public class Target2PaymentRequestDTO extends BasePaymentRequestDTO {

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

    @Schema(description = "Regulatory reporting information")
    private String regulatoryReporting;

    @Schema(description = "Charge bearer (DEBT, CRED, SHAR)", example = "SHAR")
    private String chargeBearer;

    @Schema(description = "Instruction priority (NORM, HIGH, URGT)", example = "URGT")
    private String instructionPriority;

    @Schema(description = "Settlement priority (NORM, HIGH, URGT)", example = "HIGH")
    private String settlementPriority;

    @Schema(description = "Settlement time indication (CLSTIME, TILTIME, REJTIME)", example = "TILTIME")
    private String settlementTimeIndication;

    @Schema(description = "Settlement time (HH:MM:SS+/-ZZ:ZZ)", example = "14:00:00+01:00")
    private String settlementTime;

    @Schema(description = "TARGET2 message type (pacs.008, pacs.009, etc.)", example = "pacs.008")
    private String messageType;

    @Schema(description = "TARGET2 service level", example = "SEPA")
    private String serviceLevel;

    @Schema(description = "TARGET2 clearing system", example = "TGT")
    private String clearingSystem;

    @Schema(description = "TARGET2 member identification", example = "DEUTDEFFXXX")
    private String memberIdentification;
}
