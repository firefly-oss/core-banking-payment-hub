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