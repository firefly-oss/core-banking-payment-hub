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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Result of a payment simulation operation.
 * Extends BasePaymentResultDTO with simulation-specific fields.
 *
 * The simulation operation is used to:
 * 1. Trigger SCA (Strong Customer Authentication) delivery
 * 2. Provide information like fees, estimated dates, etc.
 * 3. Allow subsequent execute, schedule, or cancel operations with the SCA code and the same information
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Result of a payment simulation operation")
public class PaymentSimulationResultDTO extends BasePaymentResultDTO {

    @Schema(description = "Estimated execution date if the payment were to be executed", example = "2023-12-31")
    private LocalDate estimatedExecutionDate;

    @Schema(description = "Estimated settlement date when funds would be available to the beneficiary", example = "2024-01-02")
    private LocalDate estimatedSettlementDate;

    @Schema(description = "Estimated fee for executing the payment", example = "2.50")
    private BigDecimal estimatedFee;

    @Schema(description = "Currency of the estimated fee (ISO 4217 code)", example = "EUR")
    private String feeCurrency;

    @Schema(description = "Estimated exchange rate if currency conversion is involved", example = "1.1050")
    private BigDecimal estimatedExchangeRate;

    @Schema(description = "Flag indicating if the payment would be likely to succeed if executed")
    private boolean feasible;

    @Schema(description = "Validation issues or warnings that would not prevent execution but should be noted")
    private String[] warnings;

    @Schema(description = "Flag indicating if SCA delivery was triggered during simulation")
    private boolean scaDeliveryTriggered;

    @Schema(description = "Timestamp when the SCA was delivered")
    private LocalDateTime scaDeliveryTimestamp;

    @Schema(description = "Method used for SCA delivery (SMS, EMAIL, APP, etc.)")
    private String scaDeliveryMethod;

    @Schema(description = "Recipient of the SCA delivery (masked phone number, email, etc.)")
    private String scaDeliveryRecipient;

    @Schema(description = "Expiry timestamp for the delivered SCA code")
    private LocalDateTime scaExpiryTimestamp;

    @Schema(description = "Unique reference ID for this simulation that should be used in subsequent operations")
    private String simulationReference;
}
