# Biometric Authentication

This document describes the biometric authentication capabilities in the Core Banking Payment Hub.

## Overview

Biometric authentication provides a more secure and user-friendly alternative to traditional authentication methods like SMS or email codes. The Core Banking Payment Hub supports multiple biometric authentication methods, allowing users to authenticate payment operations using their physical characteristics.

## Supported Biometric Methods

The following biometric authentication methods are supported:

| Method | Code | Description |
|--------|------|-------------|
| Fingerprint | `BIOMETRIC_FINGERPRINT` | Uses the device's fingerprint sensor |
| Facial Recognition | `BIOMETRIC_FACE` | Uses the device's camera for facial recognition |
| Voice Recognition | `BIOMETRIC_VOICE` | Uses the device's microphone for voice recognition |
| Iris Scan | `BIOMETRIC_IRIS` | Uses the device's camera for iris scanning |
| Retina Scan | `BIOMETRIC_RETINA` | Uses specialized hardware for retina scanning |
| Palm Print | `BIOMETRIC_PALM` | Uses the device's camera or specialized hardware for palm print recognition |
| Vein Pattern | `BIOMETRIC_VEIN` | Uses specialized hardware for vein pattern recognition |
| Behavioral Biometrics | `BIOMETRIC_BEHAVIORAL` | Uses typing patterns, gesture analysis, and other behavioral traits |

## Implementation Details

### Client-Side Implementation

The client-side implementation of biometric authentication involves:

1. **Device Capability Detection**: Determining which biometric methods are available on the device
2. **Biometric Data Capture**: Capturing the biometric data using device APIs
3. **Local Verification**: Verifying the biometric data on the device
4. **Token Generation**: Generating a verification token to send to the server

### Server-Side Implementation

The server-side implementation of biometric authentication involves:

1. **Method Support**: Determining which biometric methods are supported for the operation
2. **Token Validation**: Validating the verification token from the client
3. **Device Verification**: Verifying that the token came from a trusted device
4. **Risk Assessment**: Assessing the risk level of the operation

## Authentication Flow

### Triggering Biometric Authentication

1. Client calls the simulation endpoint for a payment operation
2. Server determines that SCA is required and supports biometric methods
3. Server returns available biometric methods in the simulation response
4. Client selects a biometric method based on device capabilities
5. Client captures and verifies the biometric data locally
6. Client generates a verification token

### Validating Biometric Authentication

1. Client calls the execution endpoint with the payment details
2. Client includes the biometric verification token and device ID
3. Server validates the token and device ID
4. If validation succeeds, the payment is processed
5. If validation fails, the payment is rejected with an error

## Security Considerations

Biometric authentication introduces specific security considerations:

- **Biometric Data Privacy**: Biometric data is never transmitted to the server
- **Device Trust**: Only trusted devices can perform biometric authentication
- **Spoofing Prevention**: Measures are in place to prevent spoofing attacks
- **Fallback Methods**: Alternative authentication methods are available if biometric authentication fails

## Configuration

Biometric authentication can be configured through application properties:

```yaml
payment:
  sca:
    biometric:
      enabled: true
      methods:
        - BIOMETRIC_FINGERPRINT
        - BIOMETRIC_FACE
        - BIOMETRIC_VOICE
        - BIOMETRIC_IRIS
        - BIOMETRIC_RETINA
        - BIOMETRIC_PALM
        - BIOMETRIC_VEIN
        - BIOMETRIC_BEHAVIORAL
      expiry-minutes: 5  # Shorter expiry for biometric authentication
      trusted-devices-only: true
```

## Metrics and Monitoring

Biometric authentication includes comprehensive metrics collection:

- **Method Usage**: Tracks which biometric methods are being used
- **Success Rates**: Monitors success rates for each biometric method
- **Performance**: Measures the performance of biometric authentication
- **Error Rates**: Tracks error rates and failure reasons

## Testing

Testing biometric authentication can be challenging. The Core Banking Payment Hub provides:

- **Simulation Mode**: Allows testing without actual biometric hardware
- **Test Tokens**: Predefined tokens that can be used for testing
- **Mock Devices**: Mock device IDs that can be used for testing

## Client Integration

Clients integrating with the Core Banking Payment Hub should:

1. Check which biometric methods are supported by the server
2. Determine which methods are available on the device
3. Implement the appropriate biometric capture and verification
4. Generate and send the verification token to the server

## Example Request and Response

### Simulation Request

```json
{
  "amount": "1000.00",
  "currency": "EUR",
  "debtorAccount": "DE89370400440532013000",
  "creditorAccount": "FR1420041010050500013M02606",
  "reference": "INVOICE-123"
}
```

### Simulation Response with Biometric SCA

```json
{
  "simulationId": "SIM-12345",
  "fee": "2.50",
  "estimatedExecutionDate": "2023-06-15",
  "sca": {
    "required": true,
    "methods": ["SMS", "EMAIL", "BIOMETRIC_FINGERPRINT", "BIOMETRIC_FACE"],
    "challengeId": "CHL-67890"
  }
}
```

### Execution Request with Biometric SCA

```json
{
  "amount": "1000.00",
  "currency": "EUR",
  "debtorAccount": "DE89370400440532013000",
  "creditorAccount": "FR1420041010050500013M02606",
  "reference": "INVOICE-123",
  "simulationId": "SIM-12345",
  "sca": {
    "method": "BIOMETRIC_FINGERPRINT",
    "challengeId": "CHL-67890",
    "deviceId": "DEVICE-123456",
    "biometricData": "VERIFICATION-TOKEN-123456"
  }
}
```
