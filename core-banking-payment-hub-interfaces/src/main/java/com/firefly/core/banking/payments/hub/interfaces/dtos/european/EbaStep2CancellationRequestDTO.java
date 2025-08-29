package com.firefly.core.banking.payments.hub.interfaces.dtos.european;

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
 * Request for cancelling an EBA STEP2 payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for cancelling an EBA STEP2 payment")
public class EbaStep2CancellationRequestDTO {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "ID of the payment to cancel", example = "PAY-12345678", required = true)
    private String paymentId;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", required = true, example = "EBA_STEP2")
    private PaymentType paymentType;

    @Schema(description = "Original end-to-end ID of the payment", example = "E2E-12345678")
    private String endToEndId;

    @Schema(description = "Reason for cancellation", example = "DUPL")
    private String cancellationReason;

    @Schema(description = "Additional information about the cancellation", example = "Duplicate payment")
    private String additionalInformation;

    @Schema(description = "Preferred payment provider", example = "EBA_STEP2_PROVIDER")
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
