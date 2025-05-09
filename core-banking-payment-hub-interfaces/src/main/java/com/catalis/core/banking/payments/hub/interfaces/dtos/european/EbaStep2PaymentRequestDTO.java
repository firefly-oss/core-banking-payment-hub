package com.catalis.core.banking.payments.hub.interfaces.dtos.european;

import com.catalis.core.banking.payments.hub.interfaces.dtos.common.AccountInfoDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.BasePaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PartyInfoDTO;
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
 * EBA STEP2 payment request.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "EBA STEP2 payment request")
public class EbaStep2PaymentRequestDTO extends BasePaymentRequestDTO {

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

    @Schema(description = "Regulatory reporting information")
    private String regulatoryReporting;

    @Schema(description = "Charge bearer (DEBT, CRED, SHAR, SLEV)", example = "SLEV")
    private String chargeBearer;

    @Schema(description = "Instruction priority (NORM, HIGH)", example = "NORM")
    private String instructionPriority;

    @Schema(description = "Service level code (ISO 20022)", example = "SEPA")
    private String serviceLevel;

    @Schema(description = "Local instrument code (ISO 20022)", example = "INST")
    private String localInstrument;

    @Schema(description = "Category purpose code (ISO 20022)", example = "SALA")
    private String categoryPurpose;

    @Schema(description = "STEP2 service (SCT, SDD Core, SDD B2B, etc.)", example = "SCT")
    private String step2Service;

    @Schema(description = "STEP2 clearing cycle", example = "CYCLE1")
    private String clearingCycle;

    @Schema(description = "STEP2 member identification", example = "DEUTDEFFXXX")
    private String memberIdentification;
}
