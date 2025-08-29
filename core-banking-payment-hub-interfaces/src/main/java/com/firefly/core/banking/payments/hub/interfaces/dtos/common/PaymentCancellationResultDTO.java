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
 * Result of a payment cancellation operation.
 * Extends BasePaymentResultDTO with cancellation-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Result of a payment cancellation operation")
public class PaymentCancellationResultDTO extends BasePaymentResultDTO {

    @Schema(description = "Reason for cancellation", example = "Customer request")
    private String cancellationReason;

    @Schema(description = "Reference ID for the cancellation request", example = "CAN-123456789")
    private String cancellationReference;

    @Schema(description = "Timestamp when the cancellation was processed", example = "2023-12-31T12:34:56")
    private LocalDateTime cancellationTimestamp;

    @Schema(description = "Flag indicating if any fees were charged for the cancellation")
    private boolean feesCharged;

    @Schema(description = "Flag indicating if the original payment was fully cancelled")
    private boolean fullyCancelled;

    @Schema(description = "Flag indicating if the funds have been returned")
    private boolean fundsReturned;

    @Schema(description = "Additional information about the cancellation process")
    private String additionalInformation;

    // Additional fields for compatibility with provider implementations
    @Schema(description = "Date when the cancellation was processed")
    private LocalDate cancellationDate;

    /**
     * Sets the cancellation date.
     *
     * @param cancellationDate the cancellation date to set
     */
    public void setCancellationDate(LocalDate cancellationDate) {
        this.cancellationDate = cancellationDate;
        // For backward compatibility, also set the timestamp if it's not already set
        if (this.cancellationTimestamp == null && cancellationDate != null) {
            this.cancellationTimestamp = cancellationDate.atStartOfDay();
        }
    }
}
