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
 * UK BACS payment request.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "UK BACS payment request")
public class UkBacsPaymentRequestDTO extends BasePaymentRequestDTO {

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
    @Size(max = 18, message = "Remittance information must not exceed 18 characters for BACS")
    @Schema(description = "Remittance information / payment details", example = "INV12345", required = true)
    private String remittanceInformation;

    @Schema(description = "Service user number (SUN)", example = "123456")
    private String serviceUserNumber;

    @Schema(description = "Processing date (YYYY-MM-DD)", example = "2023-10-15")
    private String processingDate;

    @Schema(description = "BACS transaction code", example = "99")
    private String transactionCode;

    @Schema(description = "BACS payment type (CREDIT, DIRECT_DEBIT)", example = "CREDIT")
    private String bacsPaymentType;

    @Schema(description = "Reference for the debit leg", example = "DEBIT-REF-12345")
    private String debitReference;

    @Schema(description = "Reference for the credit leg", example = "CREDIT-REF-12345")
    private String creditReference;

    @Schema(description = "Indicates if this is a contra payment", example = "false")
    private Boolean contraPayment;
}
