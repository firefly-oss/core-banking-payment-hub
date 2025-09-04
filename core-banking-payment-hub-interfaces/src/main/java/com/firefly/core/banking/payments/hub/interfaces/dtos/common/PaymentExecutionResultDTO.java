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


package com.firefly.core.banking.payments.hub.interfaces.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Result of a payment execution operation.
 * Extends BasePaymentResultDTO with execution-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Result of a payment execution operation")
public class PaymentExecutionResultDTO extends BasePaymentResultDTO {

    @Schema(description = "Actual execution date of the payment", example = "2023-12-31")
    private LocalDate executionDate;

    @Schema(description = "Expected settlement date when funds will be available to the beneficiary", example = "2024-01-02")
    private LocalDate expectedSettlementDate;

    @Schema(description = "Transaction reference number assigned by the payment system", example = "TRN-123456789")
    private String transactionReference;

    @Schema(description = "Clearing system reference number", example = "CSR-123456789")
    private String clearingSystemReference;

    @Schema(description = "Timestamp when the payment was received by the payment system", example = "2023-12-31T12:34:56")
    private LocalDateTime receivedTimestamp;

    @Schema(description = "Flag indicating if the payment requires additional authorization")
    private boolean requiresAuthorization;

    @Schema(description = "URL or reference for authorization if required")
    private String authorizationReference;

    // Additional field for compatibility with provider implementations
    @Schema(description = "Provider-specific reference")
    private String providerReference;
}
