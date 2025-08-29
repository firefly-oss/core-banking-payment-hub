package com.firefly.core.banking.payments.hub.web.health;

import com.firefly.core.banking.payments.hub.core.config.PaymentProviderRegistry;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.providers.BasePaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Health indicator for payment providers.
 * Checks the health of all registered payment providers.
 */
@Slf4j
@Component
public class PaymentProvidersHealthIndicator implements HealthIndicator {

    private final PaymentProviderRegistry providerRegistry;

    @Autowired
    public PaymentProvidersHealthIndicator(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean allProvidersHealthy = true;

        // Check SEPA provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.SEPA_PROVIDER, "sepa", details);

        // Check SWIFT provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.SWIFT_PROVIDER, "swift", details);

        // Check ACH provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.ACH_PROVIDER, "ach", details);

        // Check UK provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.UK_PROVIDER, "uk", details);

        // Check TARGET2 provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.TARGET2_PROVIDER, "target2", details);

        // Check TIPS provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.TIPS_PROVIDER, "tips", details);

        // Check EBA STEP2 provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.EBA_STEP2_PROVIDER, "ebaStep2", details);

        // Check Internal Transfer provider
        allProvidersHealthy &= checkProviderHealth(PaymentProviderType.INTERNAL_PROVIDER, "internal", details);

        if (allProvidersHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }

    /**
     * Checks the health of a specific provider.
     *
     * @param providerType The provider type to check
     * @param detailKey The key to use in the health details map
     * @param details The health details map to update
     * @return true if the provider is healthy, false otherwise
     */
    private boolean checkProviderHealth(PaymentProviderType providerType, String detailKey, Map<String, Object> details) {
        Optional<BasePaymentProvider> providerOpt = providerRegistry.getProvider(providerType);

        if (providerOpt.isPresent()) {
            BasePaymentProvider provider = providerOpt.get();
            try {
                Instant start = Instant.now();
                boolean isHealthy = provider.isHealthy().block(); // Block for simplicity in health check
                Duration duration = Duration.between(start, Instant.now());

                Map<String, Object> providerDetails = new HashMap<>();
                providerDetails.put("status", isHealthy ? "UP" : "DOWN");
                providerDetails.put("responseTime", duration.toMillis() + "ms");
                providerDetails.put("provider", provider.getClass().getSimpleName());

                details.put(detailKey, providerDetails);
                return isHealthy;
            } catch (Exception e) {
                log.error("Error checking health of {} provider: {}", providerType, e.getMessage());

                Map<String, Object> providerDetails = new HashMap<>();
                providerDetails.put("status", "ERROR");
                providerDetails.put("error", e.getMessage());
                providerDetails.put("provider", provider.getClass().getSimpleName());

                details.put(detailKey, providerDetails);
                return false;
            }
        } else {
            // Provider not available is not considered an error
            details.put(detailKey, Map.of("status", "NOT_AVAILABLE"));
            return true;
        }
    }
}
