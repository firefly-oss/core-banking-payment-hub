package com.firefly.core.banking.payments.hub.interfaces.dtos.uk;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.AccountInfoDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.BasePaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PartyInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * UK Faster Payments Service (FPS) payment request.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "UK Faster Payments Service (FPS) payment request")
public class UkFasterPaymentRequestDTO extends BasePaymentRequestDTO {

    @Valid
    @NotNull(message = "Debtor information is required")
    @Schema(description = "Debtor (payer) information", required = true)
    private PartyInfoDTO debtor;

    @Valid
    @NotNull(message = "Debtor account information is required")
    @Schema(description = "Debtor (payer) account information", required = true)
    private AccountInfoDTO debtorAccount;

    @Valid
    @NotNull(message = "Creditor information is required")
    @Schema(description = "Creditor (payee) information", required = true)
    private PartyInfoDTO creditor;

    @Valid
    @NotNull(message = "Creditor account information is required")
    @Schema(description = "Creditor (payee) account information", required = true)
    private AccountInfoDTO creditorAccount;

    @NotBlank(message = "Remittance information is required")
    @Size(max = 140, message = "Remittance information must not exceed 140 characters")
    @Schema(description = "Remittance information / payment details", example = "Invoice 12345", required = true)
    private String remittanceInformation;

    @Schema(description = "End-to-end identification", example = "E2E-12345")
    private String endToEndId;

    @Schema(description = "Service user number (SUN)", example = "123456")
    private String serviceUserNumber;

    @Schema(description = "Payment scheme (FPS, SIP)", example = "FPS")
    private String paymentScheme;

    @Schema(description = "Regulatory reporting information")
    private String regulatoryReporting;

    @Schema(description = "Charge bearer (DEBT, CRED, SHAR)", example = "SHAR")
    private String chargeBearer;

    @Schema(description = "Instruction priority (NORM, HIGH, URGT)", example = "NORM")
    private String instructionPriority;

    @Schema(description = "Notification URL for payment status updates", example = "https://example.com/payment-notifications")
    private String notificationUrl;
}
