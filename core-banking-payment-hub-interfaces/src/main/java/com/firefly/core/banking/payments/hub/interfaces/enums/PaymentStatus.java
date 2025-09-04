/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.core.banking.payments.hub.interfaces.enums;

/**
 * Enum representing different statuses a payment can have during its lifecycle.
 */
public enum PaymentStatus {
    // Initial statuses
    CREATED("Payment created but not yet processed"),
    VALIDATED("Payment validated and ready for processing"),
    
    // Processing statuses
    PENDING("Payment is being processed"),
    SCHEDULED("Payment is scheduled for future execution"),
    
    // Final statuses
    COMPLETED("Payment has been successfully completed"),
    REJECTED("Payment was rejected"),
    FAILED("Payment processing failed"),
    CANCELLED("Payment was cancelled"),
    
    // Other statuses
    PENDING_APPROVAL("Payment is waiting for approval"),
    RETURNED("Payment was returned by the receiving bank"),
    RECALLED("Payment was recalled after submission");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines if this status is a final status (i.e., no further processing is expected).
     * 
     * @return true if this is a final status, false otherwise
     */
    public boolean isFinal() {
        return this == COMPLETED || this == REJECTED || this == FAILED || this == CANCELLED;
    }
}