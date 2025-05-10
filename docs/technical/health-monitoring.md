# Health Monitoring

## Overview

The Firefly Core Banking Payment Hub includes comprehensive health monitoring capabilities to ensure the system's operational status can be tracked and issues can be detected early. This document describes the health monitoring implementation and how it integrates with the payment hub's architecture.

## Health Check Architecture

The health monitoring system follows these key principles:

1. **Component-Level Health Checks**: Each component provides its own health status
2. **Aggregated Health Status**: Individual health statuses are aggregated into an overall system health
3. **Self-Healing Capabilities**: Some components can attempt to recover from failures
4. **Detailed Health Information**: Health checks provide detailed information about the component's status

## Key Components

### BasePaymentProvider Interface

All payment providers implement the `BasePaymentProvider` interface, which includes the `isHealthy()` method:

```java
public interface BasePaymentProvider {
    // Other methods...

    /**
     * Checks if the payment provider is healthy and operational.
     *
     * @return A Mono emitting true if the provider is healthy, false otherwise
     */
    Mono<Boolean> isHealthy();
}
```

### AbstractBasePaymentProvider Implementation

The `AbstractBasePaymentProvider` class provides a standardized implementation of the health check functionality:

```java
public abstract class AbstractBasePaymentProvider implements BasePaymentProvider {
    // Other methods...

    @Override
    public Mono<Boolean> isHealthy() {
        log.debug("Performing health check for {}", getProviderName());

        Instant start = Instant.now();

        // Default implementation checks if the provider can connect to its dependencies
        return checkProviderHealth()
                .doOnSuccess(healthy -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("Health check for {} completed: healthy={}, duration={}ms",
                            getProviderName(), healthy, duration.toMillis());
                    recordMetrics("health.check", duration.toMillis(), healthy);
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("Error during health check for {}: error={}, duration={}ms",
                            getProviderName(), error.getMessage(), duration.toMillis());
                    recordMetrics("health.check", duration.toMillis(), false);
                })
                .onErrorReturn(false);
    }

    /**
     * Checks if the provider is healthy by verifying connectivity to its dependencies.
     * This method should be overridden by subclasses to implement provider-specific health checks.
     *
     * @return A Mono emitting a boolean indicating if the provider is healthy
     */
    protected abstract Mono<Boolean> checkProviderHealth();

    /**
     * Gets the name of the provider for logging and metrics.
     *
     * @return The provider name
     */
    protected abstract String getProviderName();
}
```

### ScaProvider Interface

The SCA provider also includes health check capabilities:

```java
public interface ScaProvider {
    // Other methods...

    /**
     * Checks if the SCA provider is healthy and operational.
     *
     * @return A Mono emitting true if the provider is healthy, false otherwise
     */
    Mono<Boolean> isHealthy();
}
```

### Health Indicators

The payment hub implements Spring Boot's `HealthIndicator` interface to integrate with Spring's health endpoint:

#### PaymentProvidersHealthIndicator

Monitors the health of all payment providers:

```java
@Component
public class PaymentProvidersHealthIndicator implements ReactiveHealthIndicator {

    private final ProviderRegistry providerRegistry;

    @Override
    public Mono<Health> health() {
        return Mono.just(providerRegistry.getAllProviders())
                .flatMap(providers -> {
                    List<Mono<Health>> healthChecks = providers.stream()
                            .map(this::checkProviderHealth)
                            .collect(Collectors.toList());

                    return Flux.merge(healthChecks)
                            .collectList()
                            .map(results -> {
                                boolean allUp = results.stream()
                                        .allMatch(health -> health.getStatus() == Status.UP);

                                Health.Builder builder = allUp ? Health.up() : Health.down();

                                for (int i = 0; i < providers.size(); i++) {
                                    BasePaymentProvider provider = providers.get(i);
                                    Health health = results.get(i);

                                    builder.withDetail(provider.getClass().getSimpleName(), health);
                                }

                                return builder.build();
                            });
                });
    }

    private Mono<Health> checkProviderHealth(BasePaymentProvider provider) {
        Instant start = Instant.now();
        return provider.isHealthy()
                .map(healthy -> {
                    Duration duration = Duration.between(start, Instant.now());

                    Map<String, Object> details = new HashMap<>();
                    details.put("status", healthy ? "UP" : "DOWN");
                    details.put("responseTime", duration.toMillis() + "ms");
                    details.put("provider", provider.getClass().getSimpleName());

                    return healthy ? Health.up().withDetails(details).build() :
                                    Health.down().withDetails(details).build();
                })
                .onErrorResume(e -> {
                    Duration duration = Duration.between(start, Instant.now());

                    Map<String, Object> details = new HashMap<>();
                    details.put("status", "ERROR");
                    details.put("error", e.getMessage());
                    details.put("responseTime", duration.toMillis() + "ms");
                    details.put("provider", provider.getClass().getSimpleName());

                    return Mono.just(Health.down().withDetails(details).build());
                })
                .defaultIfEmpty(Health.unknown().build());
    }
}
```

#### ScaProviderHealthIndicator

Monitors the health of the SCA provider:

```java
@Component
public class ScaProviderHealthIndicator implements ReactiveHealthIndicator {

    private final ScaProvider scaProvider;

    @Override
    public Mono<Health> health() {
        return scaProvider.isHealthy()
                .map(healthy -> healthy ? Health.up() : Health.down())
                .onErrorResume(e -> Mono.just(Health.down(e)))
                .defaultIfEmpty(Health.unknown().build())
                .map(health -> Health.status(health.getStatus())
                        .withDetail("provider", scaProvider.getClass().getSimpleName())
                        .build());
    }
}
```

## Health Endpoint

The payment hub exposes a health endpoint at `/actuator/health` that provides the aggregated health status of all components:

```json
{
  "status": "UP",
  "components": {
    "paymentProviders": {
      "status": "UP",
      "details": {
        "DefaultSepaPaymentProvider": {
          "status": "UP"
        },
        "DefaultSwiftPaymentProvider": {
          "status": "UP"
        },
        "DefaultAchPaymentProvider": {
          "status": "UP"
        },
        "DefaultUkPaymentProvider": {
          "status": "UP"
        },
        "DefaultInternalTransferProvider": {
          "status": "UP"
        }
      }
    },
    "scaProvider": {
      "status": "UP",
      "details": {
        "provider": "DefaultScaProvider"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 328166506496,
        "threshold": 10485760,
        "exists": true
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

## Provider Implementation

Each payment provider extends the `AbstractBasePaymentProvider` class and implements the `checkProviderHealth()` method to perform provider-specific health checks:

```java
@Override
protected Mono<Boolean> checkProviderHealth() {
    // Check connectivity to external systems
    // Check configuration
    // Check dependencies

    return Mono.just(true); // Return health status
}

@Override
protected String getProviderName() {
    return "sepa"; // Return the provider name for logging and metrics
}
```

## Configuration

Health check behavior can be configured through application properties:

```yaml
management:
  endpoint:
    health:
      show-details: always
      show-components: always
  endpoints:
    web:
      exposure:
        include: health,info
  health:
    defaults:
      enabled: true
```

## Integration with Monitoring Systems

The health endpoint can be integrated with various monitoring systems:

- **Prometheus**: For metrics collection and alerting
- **Grafana**: For visualization and dashboards
- **Spring Boot Admin**: For centralized monitoring of Spring Boot applications
- **Kubernetes**: For readiness and liveness probes

## Metrics Collection

In addition to health checks, the payment hub collects detailed metrics using Micrometer:

```java
public class MetricsUtils {

    private static MeterRegistry meterRegistry;

    /**
     * Records metrics for a payment operation using Micrometer.
     */
    public static void recordPaymentMetrics(String providerName, String operation, long durationMs, boolean success) {
        // Record operation duration
        Timer timer = meterRegistry.timer("payment.provider." + providerName + "." + operation);
        timer.record(durationMs, TimeUnit.MILLISECONDS);

        // Record success/failure count
        Counter counter = meterRegistry.counter("payment.provider." + providerName + "." + operation +
            (success ? ".success" : ".failure"));
        counter.increment();
    }

    /**
     * Records metrics for an SCA operation using Micrometer.
     */
    public static void recordScaMetrics(String operation, String method, long durationMs, boolean success) {
        // Record operation duration
        Timer timer = meterRegistry.timer("sca." + operation + "." + method);
        timer.record(durationMs, TimeUnit.MILLISECONDS);

        // Record success/failure count
        Counter counter = meterRegistry.counter("sca." + operation + "." + method +
            (success ? ".success" : ".failure"));
        counter.increment();
    }
}
```

These metrics are collected for all payment operations, SCA operations, and health checks, providing detailed insights into the system's performance and health.

## Security Considerations

The health endpoint should be secured to prevent unauthorized access to sensitive information:

```yaml
management:
  endpoint:
    health:
      roles: ADMIN
  endpoints:
    web:
      exposure:
        include: health,info
```

## Best Practices

1. **Implement Detailed Health Checks**: Provide detailed information about the component's status
2. **Avoid Expensive Operations**: Health checks should be lightweight and fast
3. **Handle Timeouts**: Set appropriate timeouts for health checks
4. **Implement Circuit Breakers**: Use circuit breakers to prevent cascading failures
5. **Monitor Health Trends**: Track health status over time to identify trends
