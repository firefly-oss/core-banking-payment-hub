# SEPA Payments

## Overview

The Single Euro Payments Area (SEPA) is a payment-integration initiative of the European Union for simplification of bank transfers denominated in euro. The Firefly Core Banking Payment Hub provides comprehensive support for all SEPA payment types, ensuring compliance with the latest SEPA schemes and regulations.

## SEPA Schemes

### SEPA Credit Transfer (SCT)

SEPA Credit Transfer (SCT) is a payment instrument for making credit transfers in euro between bank accounts in the SEPA zone.

**Key Features:**
- Euro-denominated transfers within the SEPA zone
- Maximum execution time of one business day
- Shared charging principle (SHA) - sender and receiver each pay their own bank's charges
- Mandatory use of IBAN and BIC
- Structured remittance information support

**Technical Implementation:**
- ISO 20022 XML message format (pain.001.001.03)
- Support for batch processing
- End-to-end identification
- Return and recall functionality

### SEPA Instant Credit Transfer (SCT Inst)

SEPA Instant Credit Transfer (SCT Inst) is a scheme for real-time credit transfers in euro, with funds available in the beneficiary's account within seconds.

**Key Features:**
- Real-time processing (funds available within 10 seconds)
- Available 24/7/365
- Current maximum amount of €100,000 (subject to change)
- Immediate confirmation of payment execution
- Same IBAN and BIC requirements as standard SCT

**Technical Implementation:**
- ISO 20022 XML message format (pacs.008.001.02)
- Real-time messaging infrastructure
- Immediate confirmation messages
- Timeout handling for failed instant payments

### SEPA Direct Debit Core (SDD Core)

SEPA Direct Debit Core is a payment instrument for collecting funds from a debtor's account, initiated by a creditor via their bank based on a mandate given by the debtor to the creditor.

**Key Features:**
- Pre-notification to the debtor (14 calendar days before due date, unless otherwise agreed)
- Refund right of 8 weeks for authorized collections
- Refund right of 13 months for unauthorized collections
- Mandate management by the creditor

**Technical Implementation:**
- ISO 20022 XML message format (pain.008.001.02)
- Mandate reference handling
- Sequence type support (one-off, first, recurrent, final)
- R-transaction handling (refunds, returns, rejects, etc.)

### SEPA Direct Debit Business-to-Business (SDD B2B)

SEPA Direct Debit B2B is a variant of the SDD scheme specifically designed for business-to-business transactions.

**Key Features:**
- No refund right for authorized collections
- Debtor bank must verify mandate information
- Faster settlement (one business day)
- Higher security and reduced risk for creditors
- Only available to businesses (not consumers)

**Technical Implementation:**
- ISO 20022 XML message format (pain.008.001.02)
- Specific B2B scheme identification
- Mandate verification process
- Accelerated processing timelines

## SEPA Payment Flows

### SCT Flow

1. **Initiation**: The debtor initiates a credit transfer through their bank
2. **Validation**: The debtor bank validates the payment information
3. **Clearing**: The payment is sent to the clearing mechanism (e.g., EBA STEP2, TIPS)
4. **Settlement**: Interbank settlement occurs
5. **Crediting**: The creditor bank credits the beneficiary's account
6. **Confirmation**: Confirmation is sent back through the chain

### SCT Inst Flow

1. **Initiation**: The debtor initiates an instant credit transfer
2. **Real-time Processing**: The debtor bank processes the payment in real-time
3. **Instant Clearing**: The payment is sent to the instant payment clearing mechanism (e.g., TIPS, RT1)
4. **Instant Settlement**: Real-time settlement occurs
5. **Immediate Crediting**: The creditor bank immediately credits the beneficiary's account
6. **Confirmation**: Real-time confirmation is sent back to the debtor

### SDD Flow

1. **Mandate Setup**: The debtor provides a mandate to the creditor
2. **Pre-notification**: The creditor notifies the debtor about the upcoming collection
3. **Collection Initiation**: The creditor submits the collection to their bank
4. **Interbank Processing**: The collection is processed through the clearing mechanism
5. **Debiting**: The debtor bank debits the debtor's account
6. **Settlement**: Interbank settlement occurs
7. **Crediting**: The creditor bank credits the creditor's account

## SEPA Data Requirements

### Mandatory Fields

#### For SCT and SCT Inst:
- Debtor name
- Debtor IBAN
- Debtor BIC (optional for SEPA-zone payments)
- Creditor name
- Creditor IBAN
- Creditor BIC (optional for SEPA-zone payments)
- Amount (EUR)
- Remittance information

#### For SDD Core and SDD B2B:
- Creditor name
- Creditor IBAN
- Creditor BIC
- Creditor ID
- Mandate reference
- Mandate signature date
- Sequence type
- Debtor name
- Debtor IBAN
- Debtor BIC (optional for SEPA-zone payments)
- Amount (EUR)
- Due date
- Remittance information

### Character Set

SEPA payments support the Latin character set as defined in ISO 8859-1, plus the euro symbol. This includes:
- a-z, A-Z
- 0-9
- / - ? : ( ) . , ' + space
- €

## SEPA Geographic Scope

The SEPA zone includes:
- All EU member states
- Iceland, Norway, Liechtenstein, Switzerland, Monaco, San Marino, Andorra, Vatican City
- UK (post-Brexit arrangements)
- Territories considered part of the EU (e.g., Martinique, Guadeloupe, French Guiana, etc.)

## Regulatory Compliance

### PSD2 Compliance

The Payment Services Directive 2 (PSD2) introduces several requirements that affect SEPA payments:

- Strong Customer Authentication (SCA) for electronic payments
- Enhanced security measures
- Transparency of fees and exchange rates
- Faster complaint resolution
- Liability shift for unauthorized transactions

### GDPR Considerations

When processing SEPA payments, the following GDPR considerations apply:

- Lawful basis for processing payment data
- Data minimization in payment messages
- Retention periods for payment information
- Cross-border data transfer restrictions
- Data subject rights regarding payment information

## Implementation in Firefly Core Banking Payment Hub

### SepaPaymentController

The `SepaPaymentController` exposes RESTful endpoints for SEPA payment operations:

- `POST /api/v1/payments/sepa/simulate` - Simulates a SEPA payment
- `POST /api/v1/payments/sepa/execute` - Executes a SEPA payment
- `POST /api/v1/payments/sepa/cancel` - Cancels a SEPA payment
- `POST /api/v1/payments/sepa/schedule` - Schedules a SEPA payment

### SepaPaymentService

The `SepaPaymentService` provides the business logic for SEPA payment operations:

- Validation of SEPA payment requests
- Routing to appropriate SEPA payment providers
- Handling of SCA requirements
- Processing of payment responses

### SepaPaymentProvider

The `SepaPaymentProvider` interface defines the contract for SEPA payment processing:

- `simulate(SepaPaymentRequestDTO)` - Simulates a SEPA payment
- `execute(SepaPaymentRequestDTO)` - Executes a SEPA payment
- `cancel(String, String)` - Cancels a SEPA payment
- `schedule(SepaPaymentRequestDTO, String)` - Schedules a SEPA payment

## Example API Requests

### SEPA Credit Transfer Execution

```bash
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

### SEPA Direct Debit Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/sepa/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12346",
    "paymentType": "SEPA_SDD_CORE",
    "amount": 50.00,
    "currency": "EUR",
    "creditorAccount": {
      "iban": "DE89370400440532013000",
      "bic": "DEUTDEFF"
    },
    "creditorId": "DE98ZZZ09999999999",
    "mandateReference": "MANDATE-123456",
    "mandateSignatureDate": "2023-01-15",
    "sequenceType": "RCUR",
    "debtorAccount": {
      "iban": "FR1420041010050500013M02606",
      "bic": "CRLYFRPP"
    },
    "remittanceInformation": "Monthly subscription",
    "dueDate": "2023-06-01"
  }'
```

## Best Practices

1. **Always validate IBANs** using the ISO 13616 check digit algorithm
2. **Include structured remittance information** when possible for better reconciliation
3. **Implement proper error handling** for SEPA-specific error codes
4. **Monitor SEPA scheme changes** as they are updated regularly
5. **Test with different SEPA countries** as implementation details can vary
6. **Implement proper mandate management** for direct debits
7. **Consider local holidays** when calculating execution times
8. **Implement proper character set conversion** to avoid rejected payments

## Common Issues and Troubleshooting

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| Invalid IBAN | Formatting error or typo | Validate IBAN using check digit algorithm |
| Missing BIC | Required for non-SEPA zone payments | Derive BIC from IBAN or request from user |
| Payment rejection | Insufficient funds | Implement retry mechanism with notification |
| Character set issues | Non-supported characters | Implement proper character conversion |
| Mandate validation failure | Invalid mandate information | Verify mandate details with creditor |
| R-transaction handling | Various return reasons | Implement proper handling for each R-transaction type |

## Future Developments

- **SEPA Request-to-Pay** - A new SEPA scheme for requesting payments
- **Enhanced SCT Inst** - Increased transaction limits and additional features
- **SEPA Proxy Lookup** - Allowing payments using mobile numbers or email addresses
- **Enhanced data capabilities** - Support for more structured data in payments
- **One-leg out transactions** - Support for payments where only one leg is in SEPA

## References

1. European Payments Council (EPC) - [SEPA Credit Transfer Rulebook](https://www.europeanpaymentscouncil.eu/document-library/rulebooks/2023-sepa-credit-transfer-rulebook-version-10)
2. European Payments Council (EPC) - [SEPA Direct Debit Core Rulebook](https://www.europeanpaymentscouncil.eu/document-library/rulebooks/2023-sepa-direct-debit-core-rulebook-version-10)
3. European Payments Council (EPC) - [SEPA Instant Credit Transfer Rulebook](https://www.europeanpaymentscouncil.eu/document-library/rulebooks/2023-sepa-instant-credit-transfer-rulebook-version-10)
4. European Central Bank - [SEPA Migration](https://www.ecb.europa.eu/paym/integration/retail/sepa/html/index.en.html)
5. ISO 20022 - [Payment Standards](https://www.iso20022.org/payments_messages.page)
