package com.firefly.core.banking.payments.hub.interfaces.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common class for party information (debtor/creditor) in payment requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Party information for payment participants (debtor/creditor)")
public class PartyInfoDTO {

    @Schema(description = "Party ID in the system", example = "12345")
    private Long partyId;

    @NotBlank(message = "Name is required")
    @Size(max = 140, message = "Name must not exceed 140 characters")
    @Schema(description = "Party name", example = "John Doe", required = true)
    private String name;

    @Schema(description = "Party address - street and number", example = "123 Main St")
    private String addressLine1;

    @Schema(description = "Party address - additional information", example = "Apt 4B")
    private String addressLine2;

    @Schema(description = "Party address - city", example = "New York")
    private String city;

    @Schema(description = "Party address - postal code", example = "10001")
    private String postalCode;

    @Schema(description = "Party address - country code (ISO 3166-1 alpha-2)", example = "US")
    private String countryCode;

    @Schema(description = "Party identification type (e.g., PASSPORT, TAX_ID)", example = "TAX_ID")
    private String identificationType;

    @Schema(description = "Party identification value", example = "123-45-6789")
    private String identificationValue;

    @Schema(description = "Party contact phone number", example = "+1-212-555-1234")
    private String phoneNumber;

    @Schema(description = "Party email address", example = "john.doe@example.com")
    private String emailAddress;

    // For backward compatibility with tests
    @Schema(description = "Full address (deprecated, use addressLine1, addressLine2, city, postalCode instead)", example = "123 Main St, New York, NY 10001")
    private String address;

    /**
     * Sets the full address.
     *
     * @param address the full address to set
     * @return the PartyInfoDTO builder
     */
    public static class PartyInfoDTOBuilder {
        public PartyInfoDTOBuilder address(String address) {
            this.address = address;
            return this;
        }
    }
}