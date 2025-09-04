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


package com.firefly.core.banking.payments.hub.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Configuration class for payment providers.
 *
 * Note: This class has been phased out in favor of the PaymentProviderRegistry,
 * which automatically discovers and registers available providers.
 *
 * The provider implementations (DefaultSepaPaymentProvider, DefaultSwiftPaymentProvider, etc.)
 * are now annotated with @Component and automatically registered as beans.
 */
@Configuration
@ConditionalOnProperty(name = "payment.providers.config.enabled", havingValue = "false", matchIfMissing = true)
public class PaymentProvidersConfig {
    // This class is now empty as the provider beans are automatically registered
    // by Spring's component scanning due to the @Component annotation on the provider classes.
}