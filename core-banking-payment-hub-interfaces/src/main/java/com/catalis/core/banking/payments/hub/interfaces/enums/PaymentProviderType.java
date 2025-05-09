package com.catalis.core.banking.payments.hub.interfaces.enums;

/**
 * Enum representing different payment provider types that can be used for processing payments.
 * The actual provider implementations will be discovered at runtime based on available dependencies.
 */
public enum PaymentProviderType {
    SEPA_PROVIDER("SEPA payment provider"),
    SWIFT_PROVIDER("SWIFT payment provider"),
    ACH_PROVIDER("ACH payment provider for US transfers"),
    UK_PROVIDER("UK payment provider for FPS, BACS, and CHAPS"),
    TARGET2_PROVIDER("TARGET2 payment provider"),
    TIPS_PROVIDER("TIPS instant payment provider"),
    EBA_STEP2_PROVIDER("EBA STEP2 payment provider"),
    INTERNAL_PROVIDER("Internal transfer provider"),
    DEFAULT_PROVIDER("Default payment provider");

    private final String description;

    PaymentProviderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}