package com.firefly.core.banking.payments.hub.interfaces.dtos.card;

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
 * Card payment request DTO.
 * Contains fields specific to card payment operations for the card authorization center.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Card payment request containing fields for card authorization operations")
public class CardPaymentRequestDTO extends BasePaymentRequestDTO {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be between 13 and 19 digits")
    @Schema(description = "Card number (PAN)", example = "4111111111111111", required = true)
    private String cardNumber;

    @NotBlank(message = "Cardholder name is required")
    @Size(max = 100, message = "Cardholder name must not exceed 100 characters")
    @Schema(description = "Cardholder name", example = "JOHN DOE", required = true)
    private String cardholderName;

    @NotBlank(message = "Expiry month is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiry month must be in MM format (01-12)")
    @Schema(description = "Card expiry month", example = "12", required = true)
    private String expiryMonth;

    @NotBlank(message = "Expiry year is required")
    @Pattern(regexp = "^[0-9]{2}$", message = "Expiry year must be in YY format")
    @Schema(description = "Card expiry year", example = "25", required = true)
    private String expiryYear;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    @Schema(description = "Card verification value", example = "123")
    private String cvv;

    @Valid
    @NotNull(message = "Merchant information is required")
    @Schema(description = "Merchant (payee) information", required = true)
    private PartyInfoDTO merchant;

    @Schema(description = "Merchant category code", example = "5411")
    private String merchantCategoryCode;

    @Schema(description = "Terminal ID", example = "TERM001")
    private String terminalId;

    @Schema(description = "Transaction type (PURCHASE, REFUND, AUTHORIZATION)", example = "PURCHASE")
    private String transactionType;

    @Schema(description = "Card brand (VISA, MASTERCARD, AMEX, etc.)", example = "VISA")
    private String cardBrand;

    @Schema(description = "Processing mode (ONLINE, OFFLINE)", example = "ONLINE")
    private String processingMode;

    @Schema(description = "Point of service entry mode", example = "CHIP")
    private String posEntryMode;

    @Schema(description = "Additional merchant data", example = "Order #12345")
    private String merchantData;
}