package com.firefly.core.banking.payments.hub.interfaces.enums;

/**
 * Enum representing different operations that can be performed on a payment.
 */
public enum PaymentOperationType {
    SIMULATE("Simulate payment without actual execution"),
    EXECUTE("Execute payment"),
    CANCEL("Cancel an existing payment"),
    SCHEDULE("Schedule payment for future execution"),
    SIMULATE_CANCELLATION("Simulate cancellation of a payment without actual cancellation");

    private final String description;

    PaymentOperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}