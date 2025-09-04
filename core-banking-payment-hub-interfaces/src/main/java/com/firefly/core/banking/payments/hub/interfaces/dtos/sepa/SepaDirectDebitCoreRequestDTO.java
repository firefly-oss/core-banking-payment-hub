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


package com.firefly.core.banking.payments.hub.interfaces.dtos.sepa;

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
 * SEPA Direct Debit Core (SDD Core) payment request.
 * Extends SepaPaymentRequestDTO with SDD Core-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SEPA Direct Debit Core (SDD Core) payment request")
public class SepaDirectDebitCoreRequestDTO extends SepaPaymentRequestDTO {

    @NotBlank(message = "Mandate ID is required")
    @Size(max = 35, message = "Mandate ID must not exceed 35 characters")
    @Schema(description = "Mandate ID", example = "MANDATE-12345", required = true)
    private String mandateId;

    @NotBlank(message = "Mandate signature date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Mandate signature date must be in format YYYY-MM-DD")
    @Schema(description = "Mandate signature date", example = "2023-01-15", required = true)
    private String mandateSignatureDate;

    @Schema(description = "Creditor ID", example = "DE98ZZZ09999999999")
    private String creditorId;

    @Schema(description = "Sequence type (FRST, RCUR, FNAL, OOFF)", example = "FRST")
    private String sequenceType;

    @Schema(description = "Amendment indicator", example = "false")
    private Boolean amendmentIndicator;

    @Schema(description = "Original mandate ID", example = "MANDATE-12345-OLD")
    private String originalMandateId;

    @Schema(description = "Original creditor ID", example = "DE98ZZZ09999999999-OLD")
    private String originalCreditorId;

    @Schema(description = "Original debtor account IBAN", example = "DE89370400440532013000")
    private String originalDebtorAccountIban;

    @Schema(description = "Pre-notification identifier", example = "PRENOT-12345")
    private String preNotificationId;

    @Schema(description = "Pre-notification date", example = "2023-01-10")
    private String preNotificationDate;
}
