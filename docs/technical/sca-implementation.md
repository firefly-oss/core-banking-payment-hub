# Strong Customer Authentication (SCA) Implementation

This document describes the implementation of Strong Customer Authentication (SCA) in the Core Banking Payment Hub.

## Overview

Strong Customer Authentication (SCA) is a requirement introduced by the European Union's Revised Payment Services Directive (PSD2) to make online payments more secure and reduce fraud. The Core Banking Payment Hub implements SCA as a delegated operation to payment providers, ensuring that all payment operations that require authentication are properly secured.

## Architecture

The SCA implementation follows these key principles:

1. **Separation of Concerns**: SCA functionality is separated into dedicated interfaces and implementations
2. **Provider Delegation**: Payment providers can delegate SCA operations to a specialized SCA provider
3. **Consistent API**: All payment types use the same SCA interfaces and data models
4. **Extensibility**: The system can be extended with different SCA methods and providers
5. **Standardization**: All payment providers extend a common abstract base implementation
6. **Metrics Collection**: Comprehensive metrics are collected for all SCA operations

## Key Components

### ScaProvider Interface

The `ScaProvider` interface defines the contract for SCA operations:

```java
public interface ScaProvider {
    Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId);
    Mono<ScaResultDTO> validateSca(ScaDTO sca);
    Mono<Boolean> isScaRequired(String operationType, String amount, String currency, String accountId);
}
```

### BasePaymentProvider Interface

The `BasePaymentProvider` interface defines common SCA operations that all payment providers should support:

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

### DefaultScaProvider Implementation

The `DefaultScaProvider` implements the `ScaProvider` interface and provides the core SCA functionality:

- Triggering SCA challenges via different methods (SMS, email, app, biometric)
- Validating SCA responses including biometric authentication
- Determining if SCA is required based on risk factors
- Collecting metrics for all SCA operations

### Payment Provider Implementations

Each payment provider (SEPA, SWIFT, ACH, etc.) extends the `AbstractBasePaymentProvider` and implements its specific provider interface:

1. Inherits standardized SCA operations from the abstract base class
2. Implements provider-specific payment operations
3. Provides health check implementation
4. Benefits from consistent error handling and metrics collection

## SCA Flow

### Simulation Phase

1. Client calls the simulation endpoint for a payment operation
2. Payment provider determines if SCA is required
3. If required, SCA challenge is triggered and delivered to the user
4. Simulation response includes SCA details (challenge ID, delivery method, etc.)

### Execution Phase

1. Client calls the execution endpoint with the same payment details
2. Client includes SCA response (challenge ID and authentication code)
3. Payment provider validates the SCA response
4. If validation succeeds, the payment is processed
5. If validation fails, the payment is rejected with an SCA error

## SCA Data Models

### ScaDTO

The `ScaDTO` represents the SCA information provided by the client:

```java
@Data
@Builder
public class ScaDTO {
    private String method;  // SMS, EMAIL, APP, BIOMETRIC_FINGERPRINT, BIOMETRIC_FACE, BIOMETRIC_VOICE
    private String recipient;
    private String authenticationCode;
    private String challengeId;
    private Boolean required;
    private Boolean completed;
    private String challengeTimestamp;
    private String expiryTimestamp;
    private String biometricData;  // Additional data for biometric authentication
    private String deviceId;       // Device ID for biometric authentication
}
```

### ScaResultDTO

The `ScaResultDTO` represents the result of an SCA operation:

```java
@Data
@Builder
public class ScaResultDTO {
    private boolean success;
    private String method;
    private String challengeId;
    private LocalDateTime verificationTimestamp;
    private String errorCode;
    private String errorMessage;
    private Integer attemptCount;
    private Integer maxAttempts;
    private boolean expired;
    private LocalDateTime expiryTimestamp;
    private String message;
    private String authenticationMethod;
    private LocalDateTime authenticationTimestamp;
}
```

## Health Monitoring

The SCA implementation includes health checks to monitor the operational status:

- `ScaProviderHealthIndicator`: Monitors the health of the SCA provider
- `PaymentProvidersHealthIndicator`: Monitors the health of all payment providers, including their SCA capabilities

## Metrics Collection

The SCA implementation includes comprehensive metrics collection using Micrometer:

- **Operation Timing**: Duration of SCA operations (trigger, validate, requirement check)
- **Success/Failure Rates**: Success and failure counts for each operation
- **Method-specific Metrics**: Metrics broken down by authentication method
- **Biometric Authentication Metrics**: Specific metrics for biometric authentication methods

Metrics are exposed through the `/actuator/prometheus` endpoint and can be visualized using tools like Grafana.

## Configuration

SCA behavior can be configured through application properties:

```yaml
payment:
  sca:
    enabled: true
    default-method: SMS
    expiry-minutes: 15
    max-attempts: 3
    threshold-amount: 500.00
    biometric:
      enabled: true
      methods:
        - BIOMETRIC_FINGERPRINT
        - BIOMETRIC_FACE
        - BIOMETRIC_VOICE
      expiry-minutes: 5  # Shorter expiry for biometric authentication
```

## Testing

The SCA implementation includes test utilities to simulate SCA challenges and responses:

- For testing purposes, the code "123456" is always accepted as a valid SCA code
- Random success/failure can be simulated for other codes
- SCA can be bypassed in test environments with appropriate configuration

## Security Considerations

- SCA codes are never logged in production environments
- Phone numbers and other recipient identifiers are masked in logs
- SCA challenges expire after a configurable time period
- Failed attempts are limited to prevent brute force attacks
- Biometric data is never stored or transmitted in plain text
- Device verification is required for biometric authentication
- Biometric authentication has stricter security requirements

## Biometric Authentication

The SCA implementation supports the following biometric authentication methods:

- **Fingerprint Authentication**: Uses the device's fingerprint sensor
- **Facial Recognition**: Uses the device's camera for facial recognition
- **Voice Recognition**: Uses the device's microphone for voice recognition

Biometric authentication offers several advantages:

- **Improved User Experience**: No need to enter codes manually
- **Higher Security**: More difficult to forge than traditional methods
- **Faster Authentication**: Quicker than entering codes

Implementation details:

- Biometric authentication is handled by the client device
- The server receives only a verification token, not the actual biometric data
- Additional device verification is performed to prevent spoofing
- Metrics are collected to monitor the effectiveness of biometric authentication
