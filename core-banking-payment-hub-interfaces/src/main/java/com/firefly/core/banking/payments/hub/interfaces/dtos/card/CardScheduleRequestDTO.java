package com.firefly.core.banking.payments.hub.interfaces.dtos.card;

import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request for scheduling a card payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for scheduling a card payment")
public class CardScheduleRequestDTO {
    
    @Valid
    @NotNull(message = "Payment request is required")
    @Schema(description = "The card payment request to schedule", required = true)
    private CardPaymentRequestDTO paymentRequest;
    
    @NotNull(message = "Execution date is required")
    @Future(message = "Execution date must be in the future")
    @Schema(description = "Date when the payment should be executed", example = "2023-12-31", required = true)
    private LocalDate executionDate;
    
    @Schema(description = "Time when the payment should be executed", example = "14:30:00")
    private LocalTime executionTime;
    
    @Schema(description = "Recurrence pattern (CRON expression)", example = "0 0 1 1 * ?")
    private String recurrencePattern;
    
    @Schema(description = "End date for recurring payments", example = "2024-12-31")
    private LocalDate recurrenceEndDate;
    
    @Schema(description = "Maximum number of recurrences", example = "12")
    private Integer maxRecurrences;
    
    @Schema(description = "Description of the schedule", example = "Monthly subscription payment")
    private String description;
    
    @Schema(description = "Preferred payment provider", example = "VISA_PROCESSOR")
    private PaymentProviderType preferredProvider;
    
    @Schema(description = "Flag indicating if the schedule can be modified after creation")
    private Boolean modifiable;
    
    @Schema(description = "Flag indicating if the schedule can be cancelled after creation")
    private Boolean cancellable;

    @Schema(description = "Flag indicating if card authorization should be renewed for each occurrence")
    private Boolean renewAuthorizationOnRecurrence;
}