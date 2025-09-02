package com.firefly.core.banking.payments.hub.interfaces.dtos.card;

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
 * Request for cancelling a card payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for cancelling a card payment")
public class CardCancellationRequestDTO {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "ID of the card payment to cancel", example = "CARD-PAY-123456789", required = true)
    private String paymentId;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", required = true)
    private PaymentType paymentType;

    @Schema(description = "Original end-to-end ID of the payment", example = "E2E-CARD-12345")
    private String endToEndId;

    @Schema(description = "Reason for cancellation", example = "FRAUD")
    private String cancellationReason;

    @Schema(description = "Additional information about the cancellation", example = "Suspected fraudulent transaction")
    private String additionalInformation;

    @Schema(description = "Preferred payment provider", example = "VISA_PROCESSOR")
    private PaymentProviderType preferredProvider;

    @Schema(description = "Original transaction reference", example = "TRN-CARD-123456789")
    private String originalTransactionReference;

    @Schema(description = "Flag indicating if a partial cancellation is acceptable")
    private Boolean acceptPartialCancellation;

    @Schema(description = "Original authorization code", example = "AUTH123456")
    private String originalAuthorizationCode;

    @Schema(description = "Merchant ID from original transaction", example = "MERCH001")
    private String merchantId;

    @Schema(description = "Terminal ID from original transaction", example = "TERM001")
    private String terminalId;

    @Schema(description = "Card number (last 4 digits for verification)", example = "1111")
    private String cardNumberLast4;

    @Valid
    @Schema(description = "Strong Customer Authentication (SCA) information")
    private ScaDTO sca;

    @Schema(description = "Reference to a previous simulation, used to link the cancellation with a simulation", example = "SIM-CARD-12345678")
    private String simulationReference;
}