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
    CARD_PROVIDER("Card payment provider"),
    DEFAULT_PROVIDER("Default payment provider");

    private final String description;

    PaymentProviderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}