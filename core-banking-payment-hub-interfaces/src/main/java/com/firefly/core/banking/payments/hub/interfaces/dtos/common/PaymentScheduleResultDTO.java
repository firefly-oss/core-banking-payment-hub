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
 * Result of a payment scheduling operation.
 * Extends BasePaymentResultDTO with scheduling-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Result of a payment scheduling operation")
public class PaymentScheduleResultDTO extends BasePaymentResultDTO {

    @Schema(description = "Scheduled execution date of the payment", example = "2023-12-31")
    private LocalDate scheduledDate;

    @Schema(description = "Reference ID for the scheduled payment", example = "SCH-123456789")
    private String scheduleReference;

    @Schema(description = "Timestamp when the scheduling was processed", example = "2023-12-15T12:34:56")
    private LocalDateTime scheduledTimestamp;

    @Schema(description = "Flag indicating if the scheduled payment can be modified")
    private boolean modifiable;

    @Schema(description = "Flag indicating if the scheduled payment can be cancelled")
    private boolean cancellable;

    @Schema(description = "Recurrence pattern for recurring payments (null for one-time payments)")
    private String recurrencePattern;

    @Schema(description = "End date for recurring payments (null for one-time or open-ended recurring payments)")
    private LocalDate recurrenceEndDate;

    @Schema(description = "Additional information about the scheduling process")
    private String additionalInformation;

    // Additional fields for compatibility with provider implementations
    @Schema(description = "Scheduled execution date of the payment")
    private LocalDate scheduledExecutionDate;

    @Schema(description = "Expected settlement date of the payment")
    private LocalDate expectedSettlementDate;

    @Schema(description = "Transaction reference")
    private String transactionReference;

    @Schema(description = "Flag indicating if the payment requires authorization")
    private boolean requiresAuthorization;

    /**
     * Sets the scheduled execution date of the payment.
     *
     * @param scheduledExecutionDate the scheduled execution date to set
     */
    public void setScheduledExecutionDate(LocalDate scheduledExecutionDate) {
        this.scheduledExecutionDate = scheduledExecutionDate;
        this.scheduledDate = scheduledExecutionDate; // For backward compatibility
    }

    /**
     * Sets the expected settlement date of the payment.
     *
     * @param expectedSettlementDate the expected settlement date to set
     */
    public void setExpectedSettlementDate(LocalDate expectedSettlementDate) {
        this.expectedSettlementDate = expectedSettlementDate;
    }

    /**
     * Sets the transaction reference for the payment.
     *
     * @param transactionReference the transaction reference to set
     */
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
        this.scheduleReference = transactionReference; // For backward compatibility
    }

    /**
     * Sets whether the payment requires authorization.
     *
     * @param requiresAuthorization true if the payment requires authorization, false otherwise
     */
    public void setRequiresAuthorization(boolean requiresAuthorization) {
        this.requiresAuthorization = requiresAuthorization;
    }
}
