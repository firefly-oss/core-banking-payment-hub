# Payment Provider Standardization

This document describes the standardized structure of payment providers in the Core Banking Payment Hub.

## Overview

The Core Banking Payment Hub uses a standardized approach to payment providers, ensuring consistent behavior, error handling, and metrics collection across all payment types. This standardization is achieved through a common abstract base implementation that all payment providers extend.

## Architecture

The payment provider standardization follows these key principles:

1. **Common Base Implementation**: All payment providers extend a common abstract base class
2. **Standardized SCA Handling**: SCA operations are implemented consistently across providers
3. **Consistent Error Handling**: Error handling follows the same patterns for all providers
4. **Comprehensive Metrics Collection**: All providers collect the same metrics in a consistent way
5. **Health Monitoring**: Health checks are implemented consistently across providers

## Key Components

### BasePaymentProvider Interface

The `BasePaymentProvider` interface defines the common operations that all payment providers must support:

```java
public interface BasePaymentProvider {
    Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId);
    Mono<ScaResultDTO> validateSca(ScaDTO sca);
    Mono<Boolean> isHealthy();
}
```

### AbstractBasePaymentProvider Implementation

The `AbstractBasePaymentProvider` provides a standardized implementation of the `BasePaymentProvider` interface:

```java
public abstract class AbstractBasePaymentProvider implements BasePaymentProvider {
    protected final ScaProvider scaProvider;
    
    public AbstractBasePaymentProvider(ScaProvider scaProvider) {
        this.scaProvider = scaProvider;
    }
    
    @Override
    public Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId) {
        // Standardized implementation with metrics collection
    }
    
    @Override
    public Mono<ScaResultDTO> validateSca(ScaDTO sca) {
        // Standardized implementation with metrics collection
    }
    
    @Override
    public Mono<Boolean> isHealthy() {
        // Standardized implementation with metrics collection
    }
    
    protected abstract Mono<Boolean> checkProviderHealth();
    protected abstract String getProviderName();
}
```

### Specific Payment Provider Interfaces

Each payment type has its own interface that extends the `BasePaymentProvider` interface:

- `AchPaymentProvider`
- `SepaPaymentProvider`
- `SwiftPaymentProvider`
- `InternalTransferProvider`
- `UkPaymentProvider`
- `Target2PaymentProvider`
- `TipsPaymentProvider`
- `EbaStep2PaymentProvider`

### Payment Provider Implementations

Each payment provider implements its specific interface and extends the `AbstractBasePaymentProvider`:

```java
@Component
public class DefaultAchPaymentProvider extends AbstractBasePaymentProvider implements AchPaymentProvider {
    
    @Autowired
    public DefaultAchPaymentProvider(ScaProvider scaProvider) {
        super(scaProvider);
    }
    
    // Implement payment-specific operations
    
    @Override
    protected Mono<Boolean> checkProviderHealth() {
        // Provider-specific health check
    }
    
    @Override
    protected String getProviderName() {
        return "ach";
    }
}
```

## Metrics Collection

The standardized payment providers collect comprehensive metrics using Micrometer:

### Operation Metrics

- **Duration**: Time taken for each operation
- **Success/Failure Rates**: Success and failure counts for each operation
- **Provider-specific Metrics**: Metrics broken down by provider type

### SCA Metrics

- **SCA Operation Timing**: Duration of SCA operations (trigger, validate)
- **SCA Method Metrics**: Metrics broken down by authentication method
- **Biometric Authentication Metrics**: Specific metrics for biometric methods

### Health Metrics

- **Health Check Duration**: Time taken for health checks
- **Health Status**: Success and failure counts for health checks

## Error Handling

The standardized payment providers implement consistent error handling:

- **Logging**: All errors are logged with appropriate context
- **Metrics**: Error counts are recorded as metrics
- **Response Formatting**: Error responses follow a consistent format
- **Retry Logic**: Common retry logic for transient errors

## Health Monitoring

The standardized payment providers implement consistent health monitoring:

- **Connectivity Checks**: Verify connectivity to external systems
- **Response Time Monitoring**: Track response times for health checks
- **Detailed Health Information**: Provide detailed health status information

## Benefits

The standardized payment provider structure offers several benefits:

1. **Reduced Code Duplication**: Common functionality is implemented once
2. **Consistent Behavior**: All providers behave consistently
3. **Easier Maintenance**: Changes to common functionality only need to be made in one place
4. **Comprehensive Monitoring**: All providers are monitored in the same way
5. **Simplified Testing**: Common functionality can be tested once

## Implementation Example

Here's an example of how a payment provider is implemented using the standardized structure:

```java
@Slf4j
@Component
public class DefaultSepaPaymentProvider extends AbstractBasePaymentProvider implements SepaPaymentProvider {

    @Autowired
    public DefaultSepaPaymentProvider(ScaProvider scaProvider) {
        super(scaProvider);
    }
    
    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(SepaPaymentRequestDTO request) {
        log.info("Simulating SEPA payment: {}", request.getReference());
        
        // Implementation details...
        
        return result;
    }
    
    @Override
    protected Mono<Boolean> checkProviderHealth() {
        log.debug("Checking connectivity to SEPA payment systems");
        return Mono.just(true);
    }
    
    @Override
    protected String getProviderName() {
        return "sepa";
    }
}
```
