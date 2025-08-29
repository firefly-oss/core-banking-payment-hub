package com.firefly.core.banking.payments.hub.interfaces.dtos.ach;

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
 * Request for cancelling an ACH payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for cancelling an ACH payment")
public class AchCancellationRequestDTO {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "ID of the payment to cancel", example = "ACH-12345678", required = true)
    private String paymentId;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", required = true)
    private PaymentType paymentType;

    @Schema(description = "Reason for cancellation", example = "Customer request")
    private String cancellationReason;

    @Schema(description = "Additional information about the cancellation", example = "Payment no longer needed")
    private String additionalInformation;

    @Schema(description = "Preferred payment provider", example = "ACH_PROVIDER")
    private PaymentProviderType preferredProvider;

    @Schema(description = "Original transaction reference", example = "TRN-12345678")
    private String originalTransactionReference;

    @Schema(description = "Flag indicating if a partial cancellation is acceptable")
    private Boolean acceptPartialCancellation;

    @Valid
    @Schema(description = "Strong Customer Authentication (SCA) information")
    private ScaDTO sca;

    @Schema(description = "Reference to a previous simulation, used to link the cancellation with a simulation", example = "SIM-12345678")
    private String simulationReference;
}
