package com.catalis.core.banking.payments.hub.web.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for metrics collection and reporting.
 * Sets up Micrometer with appropriate tags and filters.
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizes the meter registry with common tags and filters.
     *
     * @return A customizer for the meter registry
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // Add common tags to all metrics
            registry.config()
                    .commonTags("application", "payment-hub")
                    // Add a filter to deny metrics that match the pattern
                    .meterFilter(MeterFilter.deny(id -> 
                        id.getName().startsWith("jvm.") && 
                        id.getTag("region") != null && 
                        id.getTag("region").equals("test")))
                    // Configure SLA thresholds for payment operations
                    .meterFilter(
                        new MeterFilter() {
                            @Override
                            public MeterFilter.DistributionStatisticConfig configure(io.micrometer.core.instrument.Meter.Id id, MeterFilter.DistributionStatisticConfig config) {
                                if (id.getName().startsWith("payment.provider.")) {
                                    return DistributionStatisticConfig.builder()
                                            .percentiles(0.5, 0.95, 0.99)
                                            .serviceLevelObjectives(
                                                    50.0, // 50ms
                                                    100.0, // 100ms
                                                    500.0, // 500ms
                                                    1000.0 // 1s
                                            )
                                            .build()
                                            .merge(config);
                                }
                                if (id.getName().startsWith("sca.")) {
                                    return DistributionStatisticConfig.builder()
                                            .percentiles(0.5, 0.95, 0.99)
                                            .serviceLevelObjectives(
                                                    100.0, // 100ms
                                                    500.0, // 500ms
                                                    1000.0, // 1s
                                                    5000.0 // 5s
                                            )
                                            .build()
                                            .merge(config);
                                }
                                return config;
                            }
                        });
        };
    }
}
