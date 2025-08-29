package com.firefly.core.banking.payments.hub.interfaces.dtos.ach;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.AccountInfoDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.BasePaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PartyInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for ACH payment requests.
 * Contains common fields for all ACH payment types.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Base ACH payment request containing common fields for all ACH payment types")
public class AchTransferRequestDTO extends BasePaymentRequestDTO {

    @Valid
    @NotNull(message = "Originator information is required")
    @Schema(description = "Originator (sender) information", required = true)
    private PartyInfoDTO originator;

    @Valid
    @NotNull(message = "Originator account information is required")
    @Schema(description = "Originator (sender) account information", required = true)
    private AccountInfoDTO originatorAccount;

    @Valid
    @NotNull(message = "Receiver information is required")
    @Schema(description = "Receiver (beneficiary) information", required = true)
    private PartyInfoDTO receiver;

    @Valid
    @NotNull(message = "Receiver account information is required")
    @Schema(description = "Receiver (beneficiary) account information", required = true)
    private AccountInfoDTO receiverAccount;

    @NotBlank(message = "ABA routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "ABA routing number must be 9 digits")
    @Schema(description = "ABA routing number of the receiving financial institution", example = "021000021", required = true)
    private String receivingBankRoutingNumber;

    @NotBlank(message = "Remittance information is required")
    @Size(max = 140, message = "Remittance information must not exceed 140 characters")
    @Schema(description = "Remittance information / payment details", example = "Invoice 12345", required = true)
    private String remittanceInformation;

    @Schema(description = "SEC code (Standard Entry Class)", example = "PPD")
    private String secCode;

    @Schema(description = "Trace number", example = "12345678901234")
    private String traceNumber;

    @Schema(description = "Addenda record indicator")
    private Boolean addendaRecordIndicator;

    @Schema(description = "Addenda information")
    private String addendaInformation;

    @Schema(description = "Discretionary data", example = "PAYROLL")
    private String discretionaryData;

    @Schema(description = "Batch number", example = "1234")
    private String batchNumber;
}
