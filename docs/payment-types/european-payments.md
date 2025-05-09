# European Payments

## Overview

European payment systems provide specialized infrastructure for euro payments within the European Union and the broader European Economic Area. The Firefly Core Banking Payment Hub supports key European payment systems including TARGET2, TIPS, and EBA STEP2, ensuring compliance with European Central Bank regulations and standards.

## European Payment Types

### TARGET2 (Trans-European Automated Real-time Gross Settlement Express Transfer System)

TARGET2 is the real-time gross settlement (RTGS) system owned and operated by the Eurosystem for large-value euro payments.

**Key Features:**
- Real-time gross settlement (RTGS) system
- Processes high-value and urgent euro payments
- Settlement finality with central bank money
- Used primarily by financial institutions
- Operated by the European Central Bank and national central banks
- Critical for monetary policy implementation

**Technical Implementation:**
- SWIFT message formats (MT103, MT202, etc.)
- Real-time processing with immediate settlement
- Liquidity management features
- Queue management for payment optimization
- Integrated with T2S (TARGET2-Securities)

### TIPS (TARGET Instant Payment Settlement)

TIPS is a pan-European instant payment settlement service that enables payment service providers to offer fund transfers in real-time and around the clock across Europe.

**Key Features:**
- Instant settlement in central bank money
- 24/7/365 availability
- Maximum 10-second processing time
- Current maximum amount of €100,000
- Pan-European reach
- Multi-currency potential (currently euro)

**Technical Implementation:**
- ISO 20022 message format
- Real-time processing
- Immediate confirmation messages
- Liquidity management features
- Reachability through SEPA Instant Credit Transfer scheme

### EBA STEP2

EBA STEP2 is a pan-European automated clearing house for mass payments in euro, operated by EBA Clearing.

**Key Features:**
- Processes both SEPA Credit Transfers and SEPA Direct Debits
- High-volume batch processing
- Multiple settlement cycles per day
- Pan-European reach
- Interoperability with local clearing systems

**Technical Implementation:**
- ISO 20022 XML message format
- Batch processing with multiple daily cycles
- Settlement through TARGET2
- Support for SEPA schemes
- Comprehensive reporting and reconciliation

## European Payment Flows

### TARGET2 Flow

1. **Initiation**: A financial institution initiates a payment in TARGET2
2. **Validation**: The system validates the payment message
3. **Settlement**: Real-time gross settlement occurs if sufficient liquidity is available
4. **Queue Management**: If liquidity is insufficient, payment is queued
5. **Confirmation**: Settlement confirmation is sent to both parties
6. **Reporting**: End-of-day reporting and reconciliation

### TIPS Flow

1. **Initiation**: A payment service provider initiates an instant payment
2. **Validation**: TIPS validates the payment message
3. **Reserve Funds**: Funds are reserved on the sender's account
4. **Confirmation Request**: Beneficiary is asked to confirm acceptance
5. **Settlement**: Immediate settlement occurs upon confirmation
6. **Notification**: Both parties receive settlement notification
7. **Timeout Handling**: Automatic rejection if confirmation not received within 20 seconds

### EBA STEP2 Flow

1. **Submission**: Payment files are submitted to STEP2
2. **Validation**: Files are validated against scheme rules
3. **Processing**: Payments are sorted and prepared for settlement
4. **Settlement**: Settlement occurs through TARGET2 at predefined times
5. **Distribution**: Payment information is distributed to receiving banks
6. **Reporting**: Comprehensive reporting for reconciliation

## European Payment Data Requirements

### Mandatory Fields for TARGET2

- Sender BIC
- Receiver BIC
- Transaction reference
- Amount and currency (EUR)
- Value date
- Ordering institution
- Beneficiary institution
- Settlement priority

### Mandatory Fields for TIPS

- Debtor name
- Debtor IBAN
- Debtor agent BIC
- Creditor name
- Creditor IBAN
- Creditor agent BIC
- Amount (EUR)
- End-to-end identifier
- Settlement priority

### Mandatory Fields for EBA STEP2

- Debtor name
- Debtor IBAN
- Debtor agent BIC
- Creditor name
- Creditor IBAN
- Creditor agent BIC
- Amount (EUR)
- Remittance information
- End-to-end identifier

## European Geographic Scope

European payment systems primarily cover:
- Euro area countries (Eurozone)
- Non-euro EU member states (optional participation)
- European Economic Area (EEA) countries
- Selected non-EEA countries with special arrangements

## Regulatory Compliance

### European Central Bank (ECB) Requirements

The ECB oversees European payment systems:

- Oversight framework for payment systems
- Liquidity requirements
- Operational resilience standards
- Access criteria
- Risk management requirements

### Payment Services Directive 2 (PSD2)

PSD2 regulates payment services throughout the European Union:

- Strong Customer Authentication (SCA) requirements
- Open Banking requirements
- Transparency of fees and terms
- Liability for unauthorized transactions
- Complaint handling procedures

### SEPA Compliance

European payments must comply with SEPA requirements:

- SEPA Credit Transfer scheme rules
- SEPA Instant Credit Transfer scheme rules
- SEPA Direct Debit scheme rules
- IBAN and BIC usage
- XML message standards

## Implementation in Firefly Core Banking Payment Hub

### EuropeanPaymentController

The `EuropeanPaymentController` exposes RESTful endpoints for European payment operations:

- `POST /api/v1/payments/european/target2/simulate` - Simulates a TARGET2 payment
- `POST /api/v1/payments/european/target2/execute` - Executes a TARGET2 payment
- `POST /api/v1/payments/european/target2/schedule` - Schedules a TARGET2 payment
- `POST /api/v1/payments/european/tips/simulate` - Simulates a TIPS payment
- `POST /api/v1/payments/european/tips/execute` - Executes a TIPS payment
- `POST /api/v1/payments/european/eba-step2/simulate` - Simulates an EBA STEP2 payment
- `POST /api/v1/payments/european/eba-step2/execute` - Executes an EBA STEP2 payment
- `POST /api/v1/payments/european/eba-step2/schedule` - Schedules an EBA STEP2 payment

### European Payment Services

The Payment Hub provides specialized services for European payment operations:

- `Target2PaymentService` - Business logic for TARGET2 payments
- `TipsPaymentService` - Business logic for TIPS payments
- `EbaStep2PaymentService` - Business logic for EBA STEP2 payments

Each service handles:
- Validation of payment requests
- Routing to appropriate payment providers
- Handling of SCA requirements
- Processing of payment responses
- Error handling and reporting

### European Payment Providers

The Payment Hub defines interfaces for European payment processing:

- `Target2PaymentProvider` - Interface for TARGET2 payment providers
- `TipsPaymentProvider` - Interface for TIPS payment providers
- `EbaStep2PaymentProvider` - Interface for EBA STEP2 payment providers

Each provider interface defines methods for:
- Simulating payments
- Executing payments
- Cancelling payments
- Scheduling payments

## European Payment Processing Schedule

### TARGET2

- **Operating Hours**: 7:00 AM to 6:00 PM CET (Monday to Friday)
- **Cut-off Time**: 5:00 PM CET for customer payments
- **Non-Processing Days**: Weekends and TARGET2 holidays

### TIPS

- **Availability**: 24/7/365
- **Processing Time**: Maximum 10 seconds
- **Settlement**: Immediate in central bank money

### EBA STEP2

- **SEPA Credit Transfer Cycles**: Multiple cycles per day
- **SEPA Instant Credit Transfer**: 24/7/365 processing
- **SEPA Direct Debit Cycles**: Multiple cycles per day
- **Cut-off Times**: Vary by cycle and payment type

## Example API Requests

### TARGET2 Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/european/target2/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12350",
    "amount": 10000.00,
    "currency": "EUR",
    "debtorName": "Deutsche Bank AG",
    "debtorAccount": {
      "iban": "DE89370400440532013000",
      "bic": "DEUTDEFF"
    },
    "creditorName": "BNP Paribas",
    "creditorAccount": {
      "iban": "FR7630006000011234567890189",
      "bic": "BNPAFRPP"
    },
    "endToEndId": "E2E-12345",
    "remittanceInformation": "Financial institution transfer",
    "settlementPriority": "HIGH"
  }'
```

### TIPS Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/european/tips/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12351",
    "amount": 1000.00,
    "currency": "EUR",
    "debtorName": "John Doe",
    "debtorAccount": {
      "iban": "DE89370400440532013000",
      "bic": "DEUTDEFF"
    },
    "creditorName": "Jane Smith",
    "creditorAccount": {
      "iban": "FR7630006000011234567890189",
      "bic": "BNPAFRPP"
    },
    "endToEndId": "E2E-12346",
    "remittanceInformation": "Urgent payment",
    "instructionPriority": "HIGH"
  }'
```

### EBA STEP2 Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/european/eba-step2/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12352",
    "amount": 5000.00,
    "currency": "EUR",
    "debtorName": "ACME Corporation",
    "debtorAccount": {
      "iban": "DE89370400440532013000",
      "bic": "DEUTDEFF"
    },
    "creditorName": "Supplier GmbH",
    "creditorAccount": {
      "iban": "AT611904300234573201",
      "bic": "ABAGATWW"
    },
    "endToEndId": "E2E-12347",
    "remittanceInformation": "Invoice payment #12345",
    "instructionPriority": "NORMAL"
  }'
```

## Payment Limits and Timeframes

### TARGET2

- **Transaction Limit**: No upper limit
- **Processing Time**: Real-time
- **Availability**: 7:00 AM to 6:00 PM CET (Monday to Friday)

### TIPS

- **Transaction Limit**: €100,000 per transaction
- **Processing Time**: Maximum 10 seconds
- **Availability**: 24/7/365

### EBA STEP2

- **Transaction Limit**: No upper limit for SCT, scheme limits for SCT Inst
- **Processing Time**: Multiple cycles per day for SCT, real-time for SCT Inst
- **Availability**: Business days for SCT, 24/7/365 for SCT Inst

## Best Practices

1. **Validate IBAN and BIC** before submission
2. **Include clear remittance information** for easy reconciliation
3. **Consider payment timing** based on the specific system's processing schedule
4. **Use the appropriate payment system** based on value, urgency, and purpose
5. **Implement proper error handling** for system-specific error codes
6. **Consider SCA requirements** for customer-initiated payments
7. **Maintain proper records** for regulatory compliance
8. **Monitor liquidity** for high-value TARGET2 payments
9. **Implement proper notification procedures** for failed payments
10. **Consider backup channels** for critical payments

## Common Issues and Troubleshooting

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| Invalid BIC | Formatting error or outdated information | Verify BIC using official directories |
| Invalid IBAN | Formatting error or typo | Validate using IBAN check algorithm |
| Payment rejection | Insufficient liquidity | Ensure sufficient funds and monitor liquidity |
| Cut-off time missed | Late submission | Consider alternative payment channels |
| Sanctions screening hit | Potential sanctions match | Review and provide additional information |
| Connectivity issues | Network or system problems | Implement backup channels and procedures |

## Future Developments

- **T2-T2S Consolidation** - New consolidated TARGET platform
- **TIPS Multi-currency** - Expansion to non-euro currencies
- **Enhanced data capabilities** - Support for more structured data
- **Cross-border instant payments** - Interoperability with non-European systems
- **ISO 20022 migration** - Complete standardization of messaging
- **Digital euro** - Potential central bank digital currency integration

## References

1. European Central Bank - [TARGET2](https://www.ecb.europa.eu/paym/target/target2/html/index.en.html)
2. European Central Bank - [TIPS](https://www.ecb.europa.eu/paym/target/tips/html/index.en.html)
3. EBA Clearing - [STEP2](https://www.ebaclearing.eu/services/step2-t-system/overview/)
4. European Central Bank - [T2-T2S Consolidation](https://www.ecb.europa.eu/paym/target/consolidation/html/index.en.html)
