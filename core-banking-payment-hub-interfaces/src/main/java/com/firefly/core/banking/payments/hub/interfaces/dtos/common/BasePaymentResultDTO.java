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

import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Base class for all payment operation results.
 * Contains common fields for all payment operation responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base payment result containing common fields for all payment operation responses")
public abstract class BasePaymentResultDTO {

    @Schema(description = "Unique identifier for the payment", example = "PAY-123456789")
    private String paymentId;

    @Schema(description = "Original request ID", example = "REQ-123456789")
    private String requestId;

    @Schema(description = "Type of payment", required = true)
    private PaymentType paymentType;

    @Schema(description = "Type of operation performed", required = true)
    private PaymentOperationType operationType;

    @Schema(description = "Current status of the payment", required = true)
    private PaymentStatus status;

    @Schema(description = "Provider that processed the payment", required = true)
    private PaymentProviderType provider;

    @Schema(description = "Timestamp when the operation was processed", required = true)
    private LocalDateTime timestamp;

    @Schema(description = "Success flag indicating if the operation was successful", required = true)
    private boolean success;

    @Schema(description = "Error code in case of failure")
    private String errorCode;

    @Schema(description = "Error message in case of failure")
    private String errorMessage;

    @Schema(description = "Provider-specific reference ID")
    private String providerReference;

    @Schema(description = "Flag indicating if SCA (Strong Customer Authentication) is required")
    private boolean scaRequired;

    @Schema(description = "Flag indicating if SCA has been completed successfully")
    private boolean scaCompleted;

    @Schema(description = "SCA verification result")
    private ScaResultDTO scaResult;
}