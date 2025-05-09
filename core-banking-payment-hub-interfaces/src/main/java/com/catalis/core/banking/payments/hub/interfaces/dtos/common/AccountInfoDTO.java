package com.catalis.core.banking.payments.hub.interfaces.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common class for account information in payment requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account information for payment participants")
public class AccountInfoDTO {

    @Schema(description = "Account ID in the system", example = "12345")
    private Long accountId;

    @Schema(description = "Account space ID in the system", example = "67890")
    private Long accountSpaceId;

    @Schema(description = "Account number", example = "1234567890")
    private String accountNumber;

    @Schema(description = "International Bank Account Number", example = "DE89370400440532013000")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "IBAN must be in valid format")
    private String iban;

    @Schema(description = "ABA Routing Number for US bank accounts", example = "021000021")
    @Pattern(regexp = "^\\d{9}$", message = "ABA routing number must be 9 digits")
    private String abaRoutingNumber;

    @Schema(description = "Bank Identifier Code (SWIFT code)", example = "DEUTDEFF")
    @Size(min = 8, max = 11, message = "BIC must be 8 or 11 characters")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "BIC must be in valid format")
    private String bic;

    @Schema(description = "Account currency (ISO 4217 code)", example = "EUR")
    private String currency;

    @Schema(description = "Account type", example = "CHECKING")
    private String accountType;

    @Schema(description = "Bank name", example = "Deutsche Bank")
    private String bankName;

    @Schema(description = "Bank country code (ISO 3166-1 alpha-2)", example = "DE")
    private String bankCountryCode;

    @Schema(description = "Bank address", example = "Taunusanlage 12, 60325 Frankfurt am Main, Germany")
    private String bankAddress;

    @Schema(description = "Flag indicating if this is an internal account within our core banking system")
    private Boolean internalAccount;
}