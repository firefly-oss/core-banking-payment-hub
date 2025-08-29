package com.firefly.core.banking.payments.hub.interfaces.enums;

/**
 * Enum representing different types of payments supported by the payment hub.
 */
public enum PaymentType {
    // SEPA payment types
    SEPA_SCT("SEPA Credit Transfer"),
    SEPA_ICT("SEPA Instant Credit Transfer"),
    SEPA_SDD("SEPA Direct Debit"),
    SEPA_SDD_CORE("SEPA Direct Debit Core"),
    SEPA_SDD_B2B("SEPA Direct Debit Business-to-Business"),
    SEPA_SCT_INST("SEPA Instant Credit Transfer"),
    SEPA_SDD_RECURRENT("SEPA Direct Debit Recurrent"),
    SEPA_SDD_ONE_OFF("SEPA Direct Debit One-off"),
    SEPA_SCT_FUTURE_DATED("SEPA Future Dated Credit Transfer"),
    SEPA_SCT_STANDING_ORDER("SEPA Standing Order Credit Transfer"),

    // SWIFT payment types
    SWIFT_MT103("SWIFT MT103 Customer Credit Transfer"),
    SWIFT_MT202("SWIFT MT202 Financial Institution Transfer"),
    SWIFT_PACS_008("SWIFT MX PACS.008 Customer Credit Transfer"),

    // US ACH payment types
    ACH_CREDIT("ACH Credit Transfer"),
    ACH_DEBIT("ACH Debit Transfer"),
    WIRE_TRANSFER("US Wire Transfer"),

    // UK payment types
    UK_FPS("UK Faster Payments Service"),
    UK_BACS("UK BACS Direct Credit"),
    UK_BACS_DIRECT_DEBIT("UK BACS Direct Debit"),
    UK_CHAPS("UK CHAPS Payment"),
    UK_STANDING_ORDER("UK Standing Order"),

    // European payment types
    TARGET2("TARGET2 Payment"),
    TIPS("TIPS Instant Payment"),
    EBA_STEP2("EBA STEP2 Payment"),

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

    /**
     * Determines if this payment type is a SEPA payment.
     *
     * @return true if this is a SEPA payment type, false otherwise
     */
    public boolean isSepa() {
        return this.name().startsWith("SEPA_");
    }

    /**
     * Determines if this payment type is a SWIFT payment.
     *
     * @return true if this is a SWIFT payment type, false otherwise
     */
    public boolean isSwift() {
        return this.name().startsWith("SWIFT_");
    }

    /**
     * Determines if this payment type is an ACH payment.
     *
     * @return true if this is an ACH payment type, false otherwise
     */
    public boolean isAch() {
        return this.name().startsWith("ACH_") || this == WIRE_TRANSFER;
    }

    /**
     * Determines if this payment type is a UK payment.
     *
     * @return true if this is a UK payment type, false otherwise
     */
    public boolean isUk() {
        return this.name().startsWith("UK_");
    }

    /**
     * Determines if this payment type is an internal transfer.
     *
     * @return true if this is an internal transfer payment type, false otherwise
     */
    public boolean isInternal() {
        return this.name().startsWith("INTERNAL_");
    }

    /**
     * Determines if this payment type is a real-time payment.
     *
     * @return true if this is a real-time payment type, false otherwise
     */
    public boolean isRealTime() {
        return this == SEPA_ICT || this == SEPA_SCT_INST ||
               this == UK_FPS || this == TIPS;
    }
}