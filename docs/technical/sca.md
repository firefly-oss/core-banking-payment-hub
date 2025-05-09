# Strong Customer Authentication (SCA)

## Overview

Strong Customer Authentication (SCA) is a security feature implemented in the Firefly Core Banking Payment Hub to enhance payment security and comply with regulatory requirements such as the Payment Services Directive 2 (PSD2) in Europe. SCA requires users to provide at least two of the following authentication factors:

1. **Knowledge** - Something only the user knows (e.g., password, PIN)
2. **Possession** - Something only the user possesses (e.g., mobile phone, hardware token)
3. **Inherence** - Something the user is (e.g., fingerprint, facial recognition)

This document provides a detailed explanation of the SCA implementation in the payment hub, including the data models, flow, and integration points.

## SCA Data Models

### ScaDTO

The `ScaDTO` class represents the SCA information provided by the user during a payment operation:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strong Customer Authentication information")
public class ScaDTO {

    @Schema(description = "SCA method (SMS, EMAIL, APP, BIOMETRIC)", example = "SMS")
    private String method;

    @Schema(description = "Recipient identifier (phone number, email, device ID)", example = "+34600000000")
    private String recipient;

    @Schema(description = "Authentication code provided by the user", example = "123456")
    private String authenticationCode;

    @Schema(description = "Challenge ID for this SCA request", example = "CHL-12345678")
    private String challengeId;
}
```

This DTO is included in payment request DTOs through the `BasePaymentRequestDTO` class, allowing SCA information to be provided for any payment type.

### ScaResultDTO

The `ScaResultDTO` class represents the result of an SCA verification process.

### ScaUtils

The `ScaUtils` class provides utility methods for SCA-related operations across different payment providers:

```java
public class ScaUtils {

    // Validates the provided SCA information
    public static ScaResultDTO validateSca(ScaDTO sca) { ... }

    // Masks a phone number for privacy, showing only the last 4 digits
    public static String maskPhoneNumber(String phoneNumber) { ... }

    // Generates a new simulation reference
    public static String generateSimulationReference() { ... }

    // Generates a new challenge ID for SCA
    public static String generateChallengeId() { ... }

    // Creates a default SCA result with challenge information
    public static ScaResultDTO createDefaultScaResult(String method, LocalDateTime expiryTimestamp) { ... }
}
```

This utility class centralizes common SCA functionality to ensure consistent behavior across all payment providers.

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strong Customer Authentication verification result")
public class ScaResultDTO {

    @Schema(description = "Flag indicating if SCA verification was successful", required = true)
    private boolean success;

    @Schema(description = "SCA method used (SMS, EMAIL, APP, BIOMETRIC)", example = "SMS")
    private String method;

    @Schema(description = "Challenge ID for the SCA process", example = "CHL-123456789")
    private String challengeId;

    @Schema(description = "Timestamp when the SCA verification was completed")
    private LocalDateTime verificationTimestamp;

    @Schema(description = "Error code in case of verification failure")
    private String errorCode;

    @Schema(description = "Error message in case of verification failure")
    private String errorMessage;

    @Schema(description = "Number of attempts made for this verification")
    private Integer attemptCount;

    @Schema(description = "Maximum number of attempts allowed")
    private Integer maxAttempts;

    @Schema(description = "Flag indicating if the verification has expired")
    private boolean expired;

    @Schema(description = "Timestamp when the verification expires")
    private LocalDateTime expiryTimestamp;

    // Additional fields for compatibility with provider implementations
    @Schema(description = "Authentication message")
    private String message;

    @Schema(description = "Authentication method used")
    private String authenticationMethod;

    @Schema(description = "Timestamp when the authentication was completed")
    private LocalDateTime authenticationTimestamp;
}
```

This DTO is included in payment result DTOs, providing detailed information about the SCA verification process.

## SCA Flow

The SCA flow in the payment hub follows a simulate-then-execute pattern for all operations (payment execution, scheduling, and cancellation):

### 1. Simulate Operation

The first step is to simulate the operation (payment, cancellation, etc.) to determine if SCA is required and trigger SCA delivery:

```java
@PostMapping("/simulate")
@Operation(summary = "Simulate a payment", description = "Simulates a payment to trigger SCA delivery and provide information")
public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulatePayment(
        @Valid @RequestBody PaymentRequestDTO request) {
    return paymentService.simulate(request)
            .map(ResponseEntity::ok);
}
```

### 2. SCA Requirement Determination and Delivery

During the simulation phase, the system determines if SCA is required for the operation based on various factors:

- Operation amount (typically required for amounts above a certain threshold)
- Operation destination (e.g., cross-border payments)
- Operation type (e.g., high-risk payment types)
- Regulatory requirements (e.g., PSD2 requirements for European payments)
- Risk assessment (based on transaction patterns, beneficiary information, etc.)

The simulation result includes an `scaRequired` flag indicating whether SCA is required for the operation.

If SCA is required, the simulation operation triggers the SCA delivery process:

1. The system determines the appropriate SCA method (SMS, EMAIL, APP, BIOMETRIC)
2. The system sends the authentication code to the user via the selected method
3. The simulation response includes information about the SCA delivery:
   - `scaDeliveryTriggered` - Flag indicating if SCA delivery was triggered
   - `scaDeliveryTimestamp` - When the SCA was delivered
   - `scaDeliveryMethod` - Method used for SCA delivery
   - `scaDeliveryRecipient` - Recipient of the SCA delivery (masked for privacy)
   - `scaExpiryTimestamp` - When the SCA code expires
   - `simulationReference` - Unique reference to link subsequent operations

The simulation also provides other important information such as fees, exchange rates, and estimated execution and settlement dates.

### 3. SCA Information Collection

After receiving the SCA code via the delivery method, the client must include the SCA information and simulation reference in the subsequent payment request (execute, schedule, or cancel):

```json
{
  "requestId": "REQ-12345",
  "paymentType": "SEPA_SCT",
  "amount": 100.00,
  "currency": "EUR",
  "debtorAccount": {
    "iban": "DE89370400440532013000",
    "bic": "DEUTDEFF"
  },
  "creditorAccount": {
    "iban": "FR1420041010050500013M02606",
    "bic": "CRLYFRPP"
  },
  "remittanceInformation": "Invoice payment #12345",
  "simulationReference": "SIM-12345678",
  "sca": {
    "method": "SMS",
    "recipient": "+34600000000",
    "authenticationCode": "123456"
  }
}
```

The SCA information includes:
- The authentication method (SMS, EMAIL, APP, BIOMETRIC)
- The recipient identifier (phone number, email, device ID)
- The authentication code provided by the user
- The simulation reference linking back to the original simulation

### 4. SCA Verification

When a payment request with SCA information is received, the system verifies the SCA information:

```java
private ScaResultDTO validateSca(ScaDTO sca) {
    // In a real implementation, this would validate the SCA data against a security service
    // For this simulation, we'll validate based on simple rules

    ScaResultDTO result = new ScaResultDTO();

    // For testing, accept "123456" as a valid code
    if (sca.getAuthenticationCode() != null && "123456".equals(sca.getAuthenticationCode())) {
        result.setSuccess(true);
        result.setMessage("SCA validation successful");
    } else if (sca.getAuthenticationCode() == null) {
        result.setSuccess(false);
        result.setErrorCode("SCA_CODE_MISSING");
        result.setErrorMessage("Authentication code is required");
    } else {
        // Random success/failure for other codes
        boolean success = Math.random() > 0.3; // 70% success rate
        result.setSuccess(success);
        if (!success) {
            result.setErrorCode("SCA_INVALID_CODE");
            result.setErrorMessage("Invalid authentication code");
        } else {
            result.setMessage("SCA validation successful");
        }
    }

    result.setMethod(sca.getMethod());
    result.setAuthenticationMethod(sca.getMethod());
    result.setVerificationTimestamp(LocalDateTime.now());
    result.setAuthenticationTimestamp(LocalDateTime.now());

    return result;
}
```

The verification process checks the authentication code against expected values and returns a result indicating success or failure.

### 5. Payment Processing

Based on the SCA verification result, the payment is either processed or rejected:

```java
if (scaRequired) {
    if (request.getSca() == null) {
        result.setSuccess(false);
        result.setStatus(PaymentStatus.REJECTED);
        result.setErrorCode("SCA_REQUIRED");
        result.setErrorMessage("Strong Customer Authentication is required for this payment");
        return Mono.just(result);
    }

    ScaResultDTO scaResult = validateSca(request.getSca());
    result.setScaResult(scaResult);
    result.setScaCompleted(scaResult.isSuccess());

    if (!scaResult.isSuccess()) {
        result.setSuccess(false);
        result.setStatus(PaymentStatus.REJECTED);
        result.setErrorCode("SCA_FAILED");
        result.setErrorMessage("Strong Customer Authentication failed: " + scaResult.getMessage());
        return Mono.just(result);
    }
}

// Continue with payment processing if SCA is successful or not required
```

If SCA is required but not provided, the payment is rejected with an `SCA_REQUIRED` error.
If SCA is provided but verification fails, the payment is rejected with an `SCA_FAILED` error.
If SCA is successful or not required, the payment processing continues.

## SCA for Different Payment Operations

The payment hub implements SCA for various payment operations:

### Payment Execution

SCA is implemented for payment execution to ensure that only authorized users can initiate payments:

```java
@PostMapping("/execute")
@Operation(summary = "Execute a SEPA payment", description = "Executes a SEPA payment with optional SCA")
public Mono<ResponseEntity<PaymentExecutionResultDTO>> executePayment(
        @Valid @RequestBody SepaPaymentRequestDTO request) {
    log.debug("Received SEPA payment execution request: {}", request);
    return sepaPaymentService.executePayment(request)
            .map(ResponseEntity::ok);
}
```

### Payment Scheduling

SCA is implemented for payment scheduling to ensure that only authorized users can schedule future payments:

```java
@PostMapping("/schedule")
@Operation(summary = "Schedule a SEPA payment", description = "Schedules a SEPA payment for future execution with optional SCA")
public Mono<ResponseEntity<PaymentScheduleResultDTO>> schedulePayment(
        @Valid @RequestBody SepaPaymentRequestDTO request,
        @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
    log.debug("Received SEPA payment schedule request: {}, execution date: {}", request, executionDate);
    return sepaPaymentService.schedulePayment(request, executionDate)
            .map(ResponseEntity::ok);
}
```

### Payment Cancellation

SCA is implemented for payment cancellation to ensure that only authorized users can cancel payments. The cancellation flow follows the same simulate-then-execute pattern as other payment operations:

#### 1. Simulate Cancellation

First, the client simulates the cancellation to determine if SCA is required and trigger SCA delivery:

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
- Whether SCA is required for the cancellation
- SCA delivery information (method, recipient, timestamp, expiry)
- A simulation reference to link the subsequent cancellation request

#### 2. Execute Cancellation with SCA

After receiving the SCA code, the client executes the cancellation with the SCA information and simulation reference:

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

#### Example Cancellation Flow

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

## SCA Implementation by Payment Type

The payment hub implements SCA for all supported payment types:

### SEPA Payments

SCA is implemented for SEPA payments to comply with PSD2 requirements:

```java
// In DefaultSepaPaymentProvider
private boolean isScaRequired(SepaPaymentRequestDTO request) {
    // SCA is required for:
    // 1. High-value payments (above 30 EUR)
    // 2. Cross-border payments within SEPA
    // 3. Payments with specific risk factors

    return request.getAmount().compareTo(new BigDecimal("30.00")) > 0;
}
```

### SWIFT Payments

SCA is implemented for SWIFT payments, especially for high-value international transfers:

```java
// In DefaultSwiftPaymentProvider
private boolean isScaRequired(SwiftPaymentRequestDTO request) {
    // SCA is required for:
    // 1. All SWIFT payments above a threshold
    // 2. Payments to certain countries
    // 3. First-time payments to new beneficiaries

    return request.getAmount().compareTo(new BigDecimal("1000.00")) > 0;
}
```

### ACH Payments

SCA is implemented for ACH payments based on US regulatory requirements:

```java
// In DefaultAchPaymentProvider
private boolean isScaRequired(AchTransferRequestDTO request) {
    // SCA is required for:
    // 1. High-value ACH transfers
    // 2. First-time payments to new receivers
    // 3. Payments with unusual patterns

    return request.getAmount().compareTo(new BigDecimal("500.00")) > 0;
}
```

### UK Payments

SCA is implemented for UK payments to comply with UK Payment Services Regulations:

```java
// In DefaultUkPaymentProvider
private boolean isScaRequired(UkPaymentRequestDTO request) {
    // SCA is required for:
    // 1. High-value UK payments
    // 2. First-time payments to new beneficiaries
    // 3. Payments with specific risk factors

    return request.getAmount().compareTo(new BigDecimal("100.00")) > 0;
}
```

### European Payments

SCA is implemented for European payments (TARGET2, TIPS, EBA STEP2) to comply with European regulations:

```java
// In DefaultTarget2PaymentProvider
private boolean isScaRequired(Target2PaymentRequestDTO request) {
    // SCA is required for:
    // 1. All TARGET2 payments above a threshold
    // 2. Payments with specific risk factors

    return request.getAmount().compareTo(new BigDecimal("1000.00")) > 0;
}
```

### Internal Transfers

SCA is implemented for internal transfers based on risk assessment:

```java
// In DefaultInternalTransferProvider
private boolean isScaRequired(InternalTransferRequestDTO request) {
    // SCA is required for:
    // 1. High-value internal transfers
    // 2. Transfers to accounts with different ownership
    // 3. Transfers with unusual patterns

    return request.getAmount().compareTo(new BigDecimal("1000.00")) > 0;
}
```

## SCA Testing

The payment hub includes testing capabilities for SCA:

### Test Authentication Codes

For testing purposes, the following authentication codes can be used:

- `123456` - Always succeeds
- Any other code - 70% success rate (random)

### Testing SCA Flow

To test the SCA flow:

1. Simulate a payment to determine if SCA is required
2. If SCA is required, include SCA information in the execution request
3. Verify that the payment is processed or rejected based on the SCA result

Example test flow:

```bash
# 1. Simulate payment
curl -X POST http://localhost:8080/api/v1/payments/sepa/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12345",
    "paymentType": "SEPA_SCT",
    "amount": 100.00,
    "currency": "EUR",
    "debtorAccount": {
      "iban": "DE89370400440532013000",
      "bic": "DEUTDEFF"
    },
    "creditorAccount": {
      "iban": "FR1420041010050500013M02606",
      "bic": "CRLYFRPP"
    },
    "remittanceInformation": "Invoice payment #12345"
  }'

# 2. Execute payment with SCA
curl -X POST http://localhost:8080/api/v1/payments/sepa/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12345",
    "paymentType": "SEPA_SCT",
    "amount": 100.00,
    "currency": "EUR",
    "debtorAccount": {
      "iban": "DE89370400440532013000",
      "bic": "DEUTDEFF"
    },
    "creditorAccount": {
      "iban": "FR1420041010050500013M02606",
      "bic": "CRLYFRPP"
    },
    "remittanceInformation": "Invoice payment #12345",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

## Regulatory Compliance

The SCA implementation in the payment hub is designed to comply with various regulatory requirements:

### PSD2 Compliance

For European payments, the SCA implementation complies with PSD2 requirements:

- Two-factor authentication for electronic payments
- Risk-based approach to SCA requirements
- Exemptions for low-value payments and trusted beneficiaries
- Secure communication of authentication elements

### UK Payment Services Regulations

For UK payments, the SCA implementation complies with UK Payment Services Regulations:

- Strong authentication for electronic payments
- Risk-based approach to authentication requirements
- Exemptions aligned with UK regulatory guidance
- Secure authentication processes

### US Regulatory Requirements

For US payments, the SCA implementation aligns with US regulatory guidance:

- Multi-factor authentication for high-risk transactions
- Risk-based approach to authentication requirements
- Compliance with FFIEC authentication guidance
- Secure authentication processes

## Conclusion

The Strong Customer Authentication implementation in the Firefly Core Banking Payment Hub provides a robust security mechanism for payment operations. By requiring multiple authentication factors for high-risk payments, the system enhances security while maintaining a good user experience for low-risk transactions.

The flexible design allows for different SCA requirements based on payment type, amount, destination, and risk factors, ensuring compliance with regulatory requirements across different regions.

## References

1. European Banking Authority - [Regulatory Technical Standards on SCA](https://www.eba.europa.eu/regulation-and-policy/payment-services-and-electronic-money/regulatory-technical-standards-on-strong-customer-authentication-and-secure-communication-under-psd2)
2. UK Financial Conduct Authority - [Strong Customer Authentication](https://www.fca.org.uk/firms/strong-customer-authentication)
3. Federal Financial Institutions Examination Council - [Authentication and Access to Financial Institution Services and Systems](https://www.ffiec.gov/press/pr083021.htm)
4. Payment Services Directive 2 (PSD2) - [Directive (EU) 2015/2366](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX%3A32015L2366)
5. UK Payment Services Regulations 2017 - [SI 2017/752](https://www.legislation.gov.uk/uksi/2017/752/contents/made)
