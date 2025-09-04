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

import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base class for all payment requests.
 * Contains common fields for all payment types.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base payment request containing common fields for all payment types")
public abstract class BasePaymentRequestDTO {

    @Schema(description = "Unique identifier for the payment request", example = "PAY-123456789")
    private String requestId;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", required = true)
    private PaymentType paymentType;

    @Schema(description = "Payment amount", example = "100.50", required = true)
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Schema(description = "Payment currency (ISO 4217 code)", example = "EUR", required = true)
    @NotNull(message = "Currency is required")
    private String currency;

    @Schema(description = "Requested execution date", example = "2023-12-31")
    private LocalDate requestedExecutionDate;

    @Schema(description = "End-to-end identifier for payment tracking", example = "E2E-REF-12345")
    private String endToEndId;

    @Schema(description = "Payment reference/description", example = "Invoice payment #12345")
    private String reference;

    @Schema(description = "Preferred payment provider", example = "TREEZOR")
    private PaymentProviderType preferredProvider;

    @Valid
    @Schema(description = "Strong Customer Authentication (SCA) information")
    private ScaDTO sca;

    @Schema(description = "Reference to a previous simulation, used to link execute/schedule/cancel operations with a simulation", example = "SIM-12345678")
    private String simulationReference;
}