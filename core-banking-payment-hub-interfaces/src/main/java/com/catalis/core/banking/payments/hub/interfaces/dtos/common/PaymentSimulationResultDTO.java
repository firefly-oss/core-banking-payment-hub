package com.catalis.core.banking.payments.hub.interfaces.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result of a payment simulation operation.
 * Extends BasePaymentResultDTO with simulation-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Result of a payment simulation operation")
public class PaymentSimulationResultDTO extends BasePaymentResultDTO {
    
    @Schema(description = "Estimated execution date if the payment were to be executed", example = "2023-12-31")
    private LocalDate estimatedExecutionDate;
    
    @Schema(description = "Estimated settlement date when funds would be available to the beneficiary", example = "2024-01-02")
    private LocalDate estimatedSettlementDate;
    
    @Schema(description = "Estimated fee for executing the payment", example = "2.50")
    private BigDecimal estimatedFee;
    
    @Schema(description = "Currency of the estimated fee (ISO 4217 code)", example = "EUR")
    private String feeCurrency;
    
    @Schema(description = "Estimated exchange rate if currency conversion is involved", example = "1.1050")
    private BigDecimal estimatedExchangeRate;
    
    @Schema(description = "Flag indicating if the payment would be likely to succeed if executed")
    private boolean feasible;
    
    @Schema(description = "Validation issues or warnings that would not prevent execution but should be noted")
    private String[] warnings;
}
