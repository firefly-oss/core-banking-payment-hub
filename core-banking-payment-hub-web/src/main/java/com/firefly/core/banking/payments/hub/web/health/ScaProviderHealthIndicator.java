package com.firefly.core.banking.payments.hub.web.health;

import com.firefly.core.banking.payments.hub.interfaces.providers.ScaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for the SCA provider.
 * Checks if the SCA provider is operational.
 */
@Slf4j
@Component
public class ScaProviderHealthIndicator implements HealthIndicator {

    private final ScaProvider scaProvider;

    @Autowired
    public ScaProviderHealthIndicator(ScaProvider scaProvider) {
        this.scaProvider = scaProvider;
    }

    @Override
    public Health health() {
        try {
            // Check if SCA provider is operational
            // For a real implementation, this might check connectivity to an SCA service
            boolean isOperational = scaProvider.isScaRequired("TEST", "100.00", "EUR", "TEST_ACCOUNT").block();
            
            if (isOperational) {
                return Health.up()
                        .withDetail("status", "operational")
                        .withDetail("provider", scaProvider.getClass().getSimpleName())
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "non-operational")
                        .withDetail("provider", scaProvider.getClass().getSimpleName())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error checking SCA provider health: {}", e.getMessage());
            return Health.down()
                    .withDetail("status", "error")
                    .withDetail("provider", scaProvider.getClass().getSimpleName())
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
