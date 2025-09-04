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
