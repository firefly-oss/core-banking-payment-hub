package com.firefly.core.banking.payments.hub.interfaces.dtos.swift;

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

import java.time.LocalDate;

/**
 * Base class for SWIFT payment requests.
 * Contains common fields for all SWIFT payment types.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Base SWIFT payment request containing common fields for all SWIFT payment types")
public class SwiftPaymentRequestDTO extends BasePaymentRequestDTO {

    @Valid
    @NotNull(message = "Ordering customer information is required")
    @Schema(description = "Ordering customer information", required = true)
    private PartyInfoDTO orderingCustomer;

    @Valid
    @NotNull(message = "Ordering institution information is required")
    @Schema(description = "Ordering institution (sender bank) information", required = true)
    private AccountInfoDTO orderingInstitution;

    @Valid
    @NotNull(message = "Beneficiary customer information is required")
    @Schema(description = "Beneficiary customer information", required = true)
    private PartyInfoDTO beneficiaryCustomer;

    @Valid
    @NotNull(message = "Beneficiary institution information is required")
    @Schema(description = "Beneficiary institution (receiver bank) information", required = true)
    private AccountInfoDTO beneficiaryInstitution;

    @Schema(description = "Intermediary institution information (if applicable)")
    private AccountInfoDTO intermediaryInstitution;

    @NotBlank(message = "Payment details are required")
    @Size(max = 140, message = "Payment details must not exceed 140 characters")
    @Schema(description = "Payment details / remittance information", example = "Invoice 12345", required = true)
    private String paymentDetails;

    @Schema(description = "Sender's reference", example = "SENDER-REF-12345")
    private String senderReference;

    @Schema(description = "Message type (e.g., MT103, MT202, PACS.008)", example = "MT103")
    private String messageType;

    @Schema(description = "Value date (settlement date)", example = "2023-12-31")
    private LocalDate valueDate;

    @Schema(description = "Charge bearer (OUR, BEN, SHA)", example = "SHA")
    private String chargeBearer;

    @Schema(description = "Regulatory reporting information")
    private String regulatoryReporting;

    @Schema(description = "Instruction code", example = "PHOB")
    private String instructionCode;

    @Schema(description = "Purpose of payment code", example = "CASH")
    private String purposeCode;
}