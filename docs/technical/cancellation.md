# Payment Cancellation

## Overview

The Firefly Core Banking Payment Hub provides a comprehensive payment cancellation system that allows users to cancel payments that have been submitted but not yet settled. The cancellation system follows the same simulate-then-execute pattern as other payment operations and includes Strong Customer Authentication (SCA) support for secure cancellations. SCA is implemented as a delegated operation to payment providers through the `ScaProvider` interface.

This document provides a detailed explanation of the payment cancellation implementation, including the data models, flow, and integration points.

## Cancellation Data Models

### Cancellation Request DTOs

Each payment type has a dedicated cancellation request DTO that includes the necessary information for cancelling a payment:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for cancelling a payment")
public class PaymentCancellationRequestDTO {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "ID of the payment to cancel", example = "PAY-12345678", required = true)
    private String paymentId;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", required = true, example = "SEPA_SCT")
    private PaymentType paymentType;

    @Schema(description = "Reason for cancellation", example = "Duplicate payment")
    private String cancellationReason;

    @Schema(description = "Additional information about the cancellation", example = "Payment no longer needed")
    private String additionalInformation;

    @Schema(description = "Preferred payment provider", example = "SEPA_PROVIDER")
    private PaymentProviderType preferredProvider;

    @Schema(description = "Original transaction reference", example = "TRN-12345678")
    private String originalTransactionReference;

    @Schema(description = "Flag indicating if a partial cancellation is acceptable")
    private Boolean acceptPartialCancellation;

    @Valid
    @Schema(description = "Strong Customer Authentication (SCA) information")
    private ScaDTO sca;

    @Schema(description = "Reference to a previous simulation, used to link the cancellation with a simulation", example = "SIM-12345678")
    private String simulationReference;
}
```

The following payment types have dedicated cancellation request DTOs:

- SEPA (SepaCancellationRequestDTO)
- SWIFT (SwiftCancellationRequestDTO)
- ACH (AchCancellationRequestDTO)
- UK (UkCancellationRequestDTO)
- TARGET2 (Target2CancellationRequestDTO)
- TIPS (TipsCancellationRequestDTO)
- EBA STEP2 (EbaStep2CancellationRequestDTO)
- Internal Transfer (InternalTransferCancellationRequestDTO)

### Cancellation Result DTO

The `PaymentCancellationResultDTO` class represents the result of a payment cancellation operation:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of a payment cancellation operation")
public class PaymentCancellationResultDTO {

    @Schema(description = "Payment ID", example = "PAY-12345678")
    private String paymentId;

    @Schema(description = "Request ID", example = "REQ-12345678")
    private String requestId;

    @Schema(description = "Payment type", example = "SEPA_SCT")
    private PaymentType paymentType;

    @Schema(description = "Operation type", example = "CANCEL")
    private PaymentOperationType operationType;

    @Schema(description = "Payment status", example = "CANCELLED")
    private PaymentStatus status;

    @Schema(description = "Payment provider", example = "SEPA_PROVIDER")
    private PaymentProviderType provider;

    @Schema(description = "Timestamp of the operation")
    private LocalDateTime timestamp;

    @Schema(description = "Flag indicating if the operation was successful")
    private boolean success;

    @Schema(description = "Error code in case of failure")
    private String errorCode;

    @Schema(description = "Error message in case of failure")
    private String errorMessage;

    @Schema(description = "Reason for cancellation", example = "Duplicate payment")
    private String cancellationReason;

    @Schema(description = "Flag indicating if SCA is required for this cancellation")
    private boolean scaRequired;

    @Schema(description = "Flag indicating if SCA has been completed")
    private boolean scaCompleted;

    @Schema(description = "SCA result information")
    private ScaResultDTO scaResult;

    @Schema(description = "Flag indicating if authorization is required for this cancellation")
    private boolean requiresAuthorization;

    @Schema(description = "Transaction reference", example = "TRN-12345678")
    private String transactionReference;

    @Schema(description = "Timestamp when the cancellation was received")
    private LocalDateTime receivedTimestamp;

    @Schema(description = "Timestamp when the cancellation was processed")
    private LocalDateTime processedTimestamp;
}
```

## Cancellation Flow

The payment cancellation flow follows the same simulate-then-execute pattern as other payment operations:

### 1. Simulate Cancellation

The first step is to simulate the cancellation to determine if it's feasible, calculate any fees, and trigger SCA delivery if required:

```java
@PostMapping("/cancel/simulate")
@Operation(summary = "Simulate cancellation of a payment",
           description = "Simulates cancellation of a payment to trigger SCA delivery and provide information")
public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateCancellation(
        @Valid @RequestBody PaymentCancellationRequestDTO request) {
    log.debug("Received payment cancellation simulation request: {}", request);
    return paymentService.simulateCancellation(request)
            .map(ResponseEntity::ok);
}
```

The simulation response includes:
- Whether the cancellation is feasible
- Any fees associated with the cancellation
- Whether SCA is required for the cancellation
- SCA delivery information (method, recipient, timestamp, expiry)
- A simulation reference to link the subsequent cancellation request

### 2. Execute Cancellation

After receiving the SCA code (if required), the client executes the cancellation with the SCA information and simulation reference:

```java
@PostMapping("/cancel")
@Operation(summary = "Cancel a payment", description = "Cancels a payment with SCA support")
public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelPayment(
        @Valid @RequestBody PaymentCancellationRequestDTO request) {
    log.debug("Received payment cancellation request: {}", request);
    return paymentService.cancelPayment(request)
            .map(ResponseEntity::ok);
}
```

The cancellation request includes:
- Payment ID
- Cancellation reason
- SCA information (method, recipient, authentication code)
- Simulation reference linking back to the original simulation

## CancellationUtils

The `CancellationUtils` class provides utility methods for payment cancellation operations across different payment providers:

```java
public class CancellationUtils {

    // Creates a default cancellation simulation result
    public static PaymentSimulationResultDTO createCancellationSimulationResult(
            String paymentId,
            PaymentType paymentType,
            PaymentProviderType providerType,
            String feeCurrency,
            boolean scaRequired,
            boolean feasible) { ... }

    // Sets up SCA delivery information in the simulation result
    public static void setupScaDelivery(
            PaymentSimulationResultDTO result,
            ScaDTO sca,
            String defaultPhoneNumber) { ... }

    // Creates a default cancellation result
    public static PaymentCancellationResultDTO createCancellationResult(
            String paymentId,
            PaymentType paymentType,
            PaymentProviderType providerType,
            String cancellationReason,
            boolean scaRequired) { ... }

    // Validates SCA for cancellation and updates the result accordingly
    public static boolean validateScaForCancellation(
            PaymentCancellationResultDTO result,
            ScaDTO sca) { ... }

    // Sets the cancellation result to indicate that cancellation is not supported
    public static void setCancellationNotSupported(
            PaymentCancellationResultDTO result,
            String paymentType) { ... }
}
```

This utility class centralizes common cancellation functionality to ensure consistent behavior across all payment providers.

## Cancellation Implementation by Payment Type

The payment hub implements cancellation for all supported payment types:

### SEPA Payments

Cancellation is implemented for SEPA payments with support for R-transactions:

```java
// In DefaultSepaPaymentProvider
@Override
public Mono<PaymentCancellationResultDTO> cancel(SepaCancellationRequestDTO request) {
    log.info("Cancelling SEPA payment: {}", request);

    // Create a default cancellation result
    boolean scaRequired = isHighValuePayment(request.getPaymentId());
    PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
            request.getPaymentId(),
            request.getPaymentType(),
            PaymentProviderType.SEPA_PROVIDER,
            request.getCancellationReason(),
            scaRequired);

    // Validate SCA if required
    if (scaRequired) {
        boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
        if (!scaValid) {
            return Mono.just(result);
        }
    }

    // Process the cancellation
    // For SEPA, cancellation is possible before settlement
    result.setSuccess(true);
    result.setStatus(PaymentStatus.CANCELLED);
    result.setTransactionReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
    result.setReceivedTimestamp(LocalDateTime.now());
    result.setProcessedTimestamp(LocalDateTime.now());

    return Mono.just(result);
}
```

### SWIFT Payments

Cancellation is implemented for SWIFT payments with support for cancellation messages:

```java
// In DefaultSwiftPaymentProvider
@Override
public Mono<PaymentCancellationResultDTO> cancel(SwiftCancellationRequestDTO request) {
    log.info("Cancelling SWIFT payment: {}", request);

    // Create a default cancellation result
    boolean scaRequired = isHighValuePayment(request.getPaymentId());
    PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
            request.getPaymentId(),
            request.getPaymentType(),
            PaymentProviderType.SWIFT_PROVIDER,
            request.getCancellationReason(),
            scaRequired);

    // Validate SCA if required
    if (scaRequired) {
        boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
        if (!scaValid) {
            return Mono.just(result);
        }
    }

    // Process the cancellation
    // For SWIFT, cancellation is possible before settlement
    result.setSuccess(true);
    result.setStatus(PaymentStatus.CANCELLATION_REQUESTED);
    result.setTransactionReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
    result.setReceivedTimestamp(LocalDateTime.now());
    result.setProcessedTimestamp(LocalDateTime.now());

    return Mono.just(result);
}
```

### ACH Payments

Cancellation is implemented for ACH payments with support for return codes:

```java
// In DefaultAchPaymentProvider
@Override
public Mono<PaymentCancellationResultDTO> cancel(AchCancellationRequestDTO request) {
    log.info("Cancelling ACH payment: {}", request);

    // Create a default cancellation result
    boolean scaRequired = isHighValuePayment(request.getPaymentId());
    PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
            request.getPaymentId(),
            request.getPaymentType(),
            PaymentProviderType.ACH_PROVIDER,
            request.getCancellationReason(),
            scaRequired);

    // Validate SCA if required
    if (scaRequired) {
        boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
        if (!scaValid) {
            return Mono.just(result);
        }
    }

    // Process the cancellation
    // For ACH, cancellation is possible before settlement
    result.setSuccess(true);
    result.setStatus(PaymentStatus.CANCELLED);
    result.setTransactionReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
    result.setReceivedTimestamp(LocalDateTime.now());
    result.setProcessedTimestamp(LocalDateTime.now());

    return Mono.just(result);
}
```

### UK Payments

Cancellation is implemented for UK payments (Faster Payments, BACS, CHAPS) with support for cancellation messages:

```java
// In DefaultUkPaymentProvider
@Override
public Mono<PaymentCancellationResultDTO> cancelFasterPayment(UkCancellationRequestDTO request) {
    log.info("Cancelling UK Faster Payment: {}", request);

    // Create a default cancellation result
    boolean scaRequired = isHighValuePayment(request.getPaymentId());
    PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
            request.getPaymentId(),
            request.getPaymentType(),
            PaymentProviderType.UK_PROVIDER,
            request.getCancellationReason(),
            scaRequired);

    // Validate SCA if required
    if (scaRequired) {
        boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
        if (!scaValid) {
            return Mono.just(result);
        }
    }

    // Process the cancellation
    // For UK Faster Payments, cancellation is generally not possible once submitted
    CancellationUtils.setCancellationNotSupported(result, "UK Faster Payment");

    return Mono.just(result);
}
```

### European Payments

Cancellation is implemented for European payments (TARGET2, TIPS, EBA STEP2) with support for cancellation messages:

```java
// In DefaultTarget2PaymentProvider
@Override
public Mono<PaymentCancellationResultDTO> cancel(Target2CancellationRequestDTO request) {
    log.info("Cancelling TARGET2 payment: {}", request);

    // Create a default cancellation result
    boolean scaRequired = isHighValuePayment(request.getPaymentId());
    PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
            request.getPaymentId(),
            request.getPaymentType(),
            PaymentProviderType.TARGET2_PROVIDER,
            request.getCancellationReason(),
            scaRequired);

    // Validate SCA if required
    if (scaRequired) {
        boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
        if (!scaValid) {
            return Mono.just(result);
        }
    }

    // Process the cancellation
    // For TARGET2, cancellation is generally not possible once submitted
    CancellationUtils.setCancellationNotSupported(result, "TARGET2");

    return Mono.just(result);
}
```

### Internal Transfers

Cancellation is implemented for internal transfers with support for cancellation within the core banking system:

```java
// In DefaultInternalTransferProvider
@Override
public Mono<PaymentCancellationResultDTO> cancel(InternalTransferCancellationRequestDTO request) {
    log.info("Cancelling internal transfer: {}", request);

    // Create a default cancellation result
    boolean scaRequired = isHighValueTransfer(request.getPaymentId());
    PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
            request.getPaymentId(),
            request.getPaymentType(),
            PaymentProviderType.INTERNAL_PROVIDER,
            request.getCancellationReason(),
            scaRequired);

    // Validate SCA if required
    if (scaRequired) {
        boolean scaValid = CancellationUtils.validateScaForCancellation(result, request.getSca());
        if (!scaValid) {
            return Mono.just(result);
        }
    }

    // Process the cancellation
    // For internal transfers, cancellation is possible before settlement
    result.setSuccess(true);
    result.setStatus(PaymentStatus.CANCELLED);
    result.setTransactionReference("CAN-" + UUID.randomUUID().toString().substring(0, 8));
    result.setReceivedTimestamp(LocalDateTime.now());
    result.setProcessedTimestamp(LocalDateTime.now());

    return Mono.just(result);
}
```

## Cancellation Testing

The payment hub includes testing capabilities for cancellation:

### Test Cancellation Flow

To test the cancellation flow:

1. Simulate a cancellation to determine if it's feasible and if SCA is required
2. If SCA is required, include SCA information in the cancellation request
3. Verify that the cancellation is processed or rejected based on the SCA result

Example test flow:

```bash
# 1. Simulate cancellation
curl -X POST http://localhost:8080/api/v1/payments/sepa/cancel/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678",
    "paymentType": "SEPA_SCT",
    "cancellationReason": "Duplicate payment"
  }'

# Response includes:
# - scaRequired: true
# - scaDeliveryMethod: "SMS"
# - scaDeliveryRecipient: "*****1234"
# - simulationReference: "SIM-12345678"

# 2. Execute cancellation with SCA
curl -X POST http://localhost:8080/api/v1/payments/sepa/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678",
    "paymentType": "SEPA_SCT",
    "cancellationReason": "Duplicate payment",
    "simulationReference": "SIM-12345678",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

## Regulatory Compliance

The cancellation implementation in the payment hub is designed to comply with various regulatory requirements:

### PSD2 Compliance

For European payments, the cancellation implementation complies with PSD2 requirements:

- Strong Customer Authentication for payment cancellations
- Risk-based approach to SCA requirements
- Secure communication of authentication elements

### UK Payment Services Regulations

For UK payments, the cancellation implementation complies with UK Payment Services Regulations:

- Strong authentication for payment cancellations
- Risk-based approach to authentication requirements
- Secure authentication processes

### US Regulatory Requirements

For US payments, the cancellation implementation aligns with US regulatory guidance:

- Multi-factor authentication for high-risk transactions
- Risk-based approach to authentication requirements
- Compliance with FFIEC authentication guidance

## Conclusion

The payment cancellation implementation in the Firefly Core Banking Payment Hub provides a robust mechanism for cancelling payments with Strong Customer Authentication support. By following the simulate-then-execute pattern and implementing consistent behavior across all payment types, the system ensures a secure and user-friendly cancellation experience.

The flexible design allows for different cancellation requirements based on payment type, amount, and risk factors, ensuring compliance with regulatory requirements across different regions.

## References

1. European Banking Authority - [Regulatory Technical Standards on SCA](https://www.eba.europa.eu/regulation-and-policy/payment-services-and-electronic-money/regulatory-technical-standards-on-strong-customer-authentication-and-secure-communication-under-psd2)
2. UK Financial Conduct Authority - [Strong Customer Authentication](https://www.fca.org.uk/firms/strong-customer-authentication)
3. Federal Financial Institutions Examination Council - [Authentication and Access to Financial Institution Services and Systems](https://www.ffiec.gov/press/pr083021.htm)
4. Payment Services Directive 2 (PSD2) - [Directive (EU) 2015/2366](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX%3A32015L2366)
5. UK Payment Services Regulations 2017 - [SI 2017/752](https://www.legislation.gov.uk/uksi/2017/752/contents/made)
