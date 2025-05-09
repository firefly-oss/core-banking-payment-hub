package com.catalis.core.banking.payments.hub.interfaces.dtos.common;

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
}
