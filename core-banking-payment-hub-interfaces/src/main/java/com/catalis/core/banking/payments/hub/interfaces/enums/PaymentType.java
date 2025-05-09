package com.catalis.core.banking.payments.hub.interfaces.enums;

/**
 * Enum representing different types of payments supported by the payment hub.
 */
public enum PaymentType {
    // SEPA payment types
    SEPA_SCT("SEPA Credit Transfer"),
    SEPA_ICT("SEPA Instant Credit Transfer"),
    SEPA_SDD("SEPA Direct Debit"),

    // SWIFT payment types
    SWIFT_MT103("SWIFT MT103 Customer Credit Transfer"),
    SWIFT_MT202("SWIFT MT202 Financial Institution Transfer"),
    SWIFT_PACS_008("SWIFT MX PACS.008 Customer Credit Transfer"),

    // US ACH payment types
    ACH_CREDIT("ACH Credit Transfer"),
    ACH_DEBIT("ACH Debit Transfer"),
    WIRE_TRANSFER("US Wire Transfer"),

    // Internal payment types
    INTERNAL_TRANSFER("Internal Transfer between accounts"),
    INTERNAL_BULK_TRANSFER("Bulk Internal Transfer");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}