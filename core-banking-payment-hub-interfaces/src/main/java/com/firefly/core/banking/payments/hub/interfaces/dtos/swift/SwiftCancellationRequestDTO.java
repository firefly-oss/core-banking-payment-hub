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

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for cancelling a SWIFT payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for cancelling a SWIFT payment")
public class SwiftCancellationRequestDTO {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "ID of the payment to cancel", example = "PAY-123456789", required = true)
    private String paymentId;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", required = true)
    private PaymentType paymentType;

    @Schema(description = "Original sender reference", example = "SENDER-REF-12345")
    private String senderReference;

    @Schema(description = "Original message type (e.g., MT103, MT202, PACS.008)", example = "MT103")
    private String messageType;

    @Schema(description = "Reason for cancellation", example = "DUPL")
    private String cancellationReason;

    @Schema(description = "Additional information about the cancellation", example = "Duplicate payment")
    private String additionalInformation;

    @Schema(description = "Preferred payment provider", example = "TREEZOR")
    private PaymentProviderType preferredProvider;

    @Schema(description = "Original transaction reference", example = "TRN-123456789")
    private String originalTransactionReference;

    @Schema(description = "Flag indicating if a partial cancellation is acceptable")
    private Boolean acceptPartialCancellation;

    @Schema(description = "Sender BIC", example = "DEUTDEFF")
    private String senderBic;

    @Schema(description = "Receiver BIC", example = "CHASUS33")
    private String receiverBic;

    @Valid
    @Schema(description = "Strong Customer Authentication (SCA) information")
    private ScaDTO sca;

    @Schema(description = "Reference to a previous simulation, used to link the cancellation with a simulation", example = "SIM-12345678")
    private String simulationReference;
}
