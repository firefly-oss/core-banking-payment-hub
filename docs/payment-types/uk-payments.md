# UK Payments

## Overview

The UK payment systems provide a comprehensive infrastructure for domestic payments within the United Kingdom. The Firefly Core Banking Payment Hub offers support for all major UK payment schemes, ensuring compliance with UK payment regulations and standards set by Pay.UK and the Bank of England.

## UK Payment Types

### Faster Payments Service (FPS)

Faster Payments is a UK banking initiative that reduces payment times between different banks' customer accounts from three working days to typically a few seconds.

**Key Features:**
- Near real-time payments (typically within seconds)
- 24/7/365 availability
- Maximum transaction limit of £1,000,000 (though individual banks may set lower limits)
- Support for mobile, internet, and telephone banking channels
- Used for both consumer and business payments

**Technical Implementation:**
- ISO 8583 message format
- Support for Single Immediate Payments (SIP)
- Support for Forward Dated Payments (FDP)
- Support for Standing Orders
- Real-time processing with immediate confirmation

### BACS (Bankers' Automated Clearing Services)

BACS is the UK's high-volume payment system that processes transactions through the BACS Direct Credit and Direct Debit schemes.

**Key Features:**
- Three-day processing cycle
- High-volume batch processing
- Lower cost compared to other payment methods
- Used for regular payments like salaries, pensions, and supplier payments
- Maximum transaction value of £20 million

**Technical Implementation:**
- Standard 18 format for Direct Credits
- AUDDIS (Automated Direct Debit Instruction Service) for Direct Debits
- Batch processing with submission deadlines
- Support for service user numbers (SUN)
- Return and recall functionality

#### BACS Direct Credit

BACS Direct Credit is used for sending payments directly into another bank account.

**Key Features:**
- Push payment mechanism
- Typically used for regular payments like salaries and pensions
- Three-day processing cycle
- Cost-effective for high volumes
- Batch processing

#### BACS Direct Debit

BACS Direct Debit is a pull payment mechanism that allows an organization to collect varying amounts from a customer's account, provided that the customer has given prior authorization.

**Key Features:**
- Pull payment mechanism
- Requires customer mandate (authorization)
- Advance notice requirement (typically 3-14 days)
- Refund rights under the Direct Debit Guarantee
- Used for regular bills and subscriptions

### CHAPS (Clearing House Automated Payment System)

CHAPS is the UK's high-value payment system that provides efficient and irrevocable same-day funds transfers.

**Key Features:**
- Real-time gross settlement (RTGS)
- Same-day settlement
- No upper limit on transaction value
- Typically used for high-value or time-critical payments
- Irrevocable once settled
- Operated by the Bank of England

**Technical Implementation:**
- SWIFT message format (MT103, MT202)
- Real-time processing
- Strict cut-off times (typically 5:40 PM UK time)
- Enhanced security and validation
- Settlement through Bank of England accounts

### UK Standing Orders

Standing Orders are regular, fixed-amount payments set up by a customer to be paid from their account at fixed intervals.

**Key Features:**
- Fixed amount and frequency
- Set up and controlled by the payer
- Typically processed through Faster Payments or BACS
- Used for regular payments like rent, mortgage, or subscriptions
- Can be amended or cancelled by the payer

**Technical Implementation:**
- Processed through Faster Payments or BACS infrastructure
- Support for various payment frequencies
- Automatic retry mechanisms for failed payments
- Cancellation and amendment functionality

## UK Payment Flows

### Faster Payments Flow

1. **Initiation**: Customer initiates a Faster Payment through their bank
2. **Validation**: Sending bank validates the payment details
3. **Submission**: Payment is submitted to the Faster Payments Service
4. **Processing**: Central infrastructure processes the payment
5. **Settlement**: Settlement occurs between banks (net settlement several times daily)
6. **Crediting**: Receiving bank credits the beneficiary's account
7. **Confirmation**: Confirmation is sent back to the sending bank

### BACS Flow

1. **Submission**: Payment files are submitted to BACS (by 10:30 PM UK time)
2. **Input Day (Day 1)**: Files are validated and processed
3. **Processing Day (Day 2)**: Files are sorted and distributed to receiving banks
4. **Output Day (Day 3)**: Funds are debited from the sender's account and credited to the receiver's account
5. **Reporting**: Reports are generated for reconciliation

### CHAPS Flow

1. **Initiation**: Customer initiates a CHAPS payment through their bank
2. **Processing**: Sending bank processes the payment in real-time
3. **Submission**: Payment is submitted to the CHAPS system
4. **Settlement**: Real-time gross settlement through Bank of England accounts
5. **Crediting**: Receiving bank credits the beneficiary's account
6. **Confirmation**: Confirmation is sent back to the sending bank

## UK Payment Data Requirements

### Mandatory Fields for UK Payments

#### For Faster Payments and BACS:
- Sender name
- Sender account number (8 digits)
- Sender sort code (6 digits)
- Recipient name
- Recipient account number (8 digits)
- Recipient sort code (6 digits)
- Payment amount
- Payment reference

#### For CHAPS:
- Sender name
- Sender account number
- Sender sort code
- Recipient name
- Recipient account number
- Recipient sort code
- Payment amount
- Payment reference
- Additional information for regulatory purposes

### Sort Codes and Account Numbers

UK payments rely on sort codes and account numbers for routing:

- **Sort Code**: 6-digit number identifying the bank and branch
  - Format: XX-XX-XX (e.g., 12-34-56)
  
- **Account Number**: 8-digit number identifying the specific account
  - Format: XXXXXXXX (e.g., 12345678)

## UK Geographic Scope

UK payment systems primarily cover:
- England
- Scotland
- Wales
- Northern Ireland
- Crown dependencies (Isle of Man, Jersey, Guernsey)

## Regulatory Compliance

### Payment Services Regulations (PSR)

The UK Payment Services Regulations implement the EU Payment Services Directive (PSD2) in UK law:

- Strong Customer Authentication (SCA) requirements
- Open Banking requirements
- Transparency of fees and terms
- Liability for unauthorized transactions
- Complaint handling procedures

### Financial Conduct Authority (FCA) Requirements

The FCA regulates payment services in the UK:

- Authorization and registration requirements
- Conduct of business requirements
- Capital and safeguarding requirements
- Reporting and notification requirements
- Complaint handling procedures

### Bank of England Oversight

The Bank of England oversees UK payment systems:

- Systemic risk management
- Operational resilience
- Settlement finality
- Liquidity management
- Access criteria

## Implementation in Firefly Core Banking Payment Hub

### UkPaymentController

The `UkPaymentController` exposes RESTful endpoints for UK payment operations:

- `POST /api/v1/payments/uk/fps/simulate` - Simulates a Faster Payment
- `POST /api/v1/payments/uk/fps/execute` - Executes a Faster Payment
- `POST /api/v1/payments/uk/bacs/simulate` - Simulates a BACS payment
- `POST /api/v1/payments/uk/bacs/execute` - Executes a BACS payment
- `POST /api/v1/payments/uk/bacs/schedule` - Schedules a BACS payment
- `POST /api/v1/payments/uk/chaps/simulate` - Simulates a CHAPS payment
- `POST /api/v1/payments/uk/chaps/execute` - Executes a CHAPS payment
- `POST /api/v1/payments/uk/chaps/schedule` - Schedules a CHAPS payment

### UkPaymentService

The `UkPaymentService` provides the business logic for UK payment operations:

- Validation of UK payment requests
- Routing to appropriate UK payment providers
- Handling of SCA requirements
- Processing of payment responses
- Error handling and reporting

### UkPaymentProvider

The `UkPaymentProvider` interface defines the contract for UK payment processing:

- `simulateFasterPayment(UkFasterPaymentRequestDTO)` - Simulates a Faster Payment
- `executeFasterPayment(UkFasterPaymentRequestDTO)` - Executes a Faster Payment
- `simulateBacsPayment(UkBacsPaymentRequestDTO)` - Simulates a BACS payment
- `executeBacsPayment(UkBacsPaymentRequestDTO)` - Executes a BACS payment
- `scheduleBacsPayment(UkBacsPaymentRequestDTO, String)` - Schedules a BACS payment
- `simulateChapsPayment(UkChapsPaymentRequestDTO)` - Simulates a CHAPS payment
- `executeChapsPayment(UkChapsPaymentRequestDTO)` - Executes a CHAPS payment
- `scheduleChapsPayment(UkChapsPaymentRequestDTO, String)` - Schedules a CHAPS payment

## UK Payment Processing Schedule

### Faster Payments

- **Availability**: 24/7/365
- **Processing Time**: Typically within seconds
- **Settlement**: Net settlement occurs multiple times per day

### BACS

- **Submission Deadline**: 10:30 PM UK time
- **Processing Cycle**: 3 business days
- **Non-Processing Days**: Weekends and UK bank holidays

### CHAPS

- **Operating Hours**: 6:00 AM to 6:00 PM UK time (Monday to Friday)
- **Cut-off Time**: 5:40 PM UK time for customer payments
- **Non-Processing Days**: Weekends and UK bank holidays

## Example API Requests

### Faster Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/uk/fps/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12349",
    "amount": 200.00,
    "currency": "GBP",
    "debtorName": "John Doe",
    "debtorAccount": {
      "sortCode": "123456",
      "accountNumber": "12345678"
    },
    "creditorName": "Jane Smith",
    "creditorAccount": {
      "sortCode": "654321",
      "accountNumber": "87654321"
    },
    "reference": "Invoice #67890",
    "paymentScheme": "FPS"
  }'
```

### BACS Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/uk/bacs/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12350",
    "amount": 1000.00,
    "currency": "GBP",
    "debtorName": "ACME Corporation",
    "debtorAccount": {
      "sortCode": "123456",
      "accountNumber": "12345678"
    },
    "creditorName": "Supplier Ltd",
    "creditorAccount": {
      "sortCode": "654321",
      "accountNumber": "87654321"
    },
    "reference": "Supplier payment",
    "paymentType": "CREDIT",
    "serviceUserNumber": "123456"
  }'
```

### CHAPS Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/uk/chaps/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12351",
    "amount": 50000.00,
    "currency": "GBP",
    "debtorName": "ACME Corporation",
    "debtorAccount": {
      "sortCode": "123456",
      "accountNumber": "12345678"
    },
    "creditorName": "Property Vendor Ltd",
    "creditorAccount": {
      "sortCode": "654321",
      "accountNumber": "87654321"
    },
    "reference": "Property purchase",
    "paymentPurpose": "PROPERTY"
  }'
```

## Payment Limits and Timeframes

### Faster Payments

- **Transaction Limit**: £1,000,000 (scheme limit, individual banks may set lower limits)
- **Processing Time**: Typically within seconds
- **Availability**: 24/7/365

### BACS

- **Transaction Limit**: £20,000,000
- **Processing Time**: 3 business days
- **Submission Deadline**: 10:30 PM UK time

### CHAPS

- **Transaction Limit**: No upper limit
- **Processing Time**: Same day
- **Cut-off Time**: 5:40 PM UK time for customer payments

## Best Practices

1. **Validate sort codes and account numbers** using modulus checking
2. **Include clear payment references** for easy reconciliation
3. **Consider payment timing** based on the specific scheme's processing schedule
4. **Use the appropriate payment scheme** based on value, urgency, and purpose
5. **Implement proper error handling** for scheme-specific error codes
6. **Consider SCA requirements** for online and mobile payments
7. **Maintain proper records** for regulatory compliance
8. **Monitor payment status** through available tracking services
9. **Implement proper notification procedures** for failed payments
10. **Consider confirmation of payee** to reduce fraud risk

## Common Issues and Troubleshooting

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| Invalid sort code | Formatting error or typo | Validate using modulus checking |
| Invalid account number | Formatting error or typo | Validate using modulus checking |
| Payment delay | Processing cut-off times | Consider using Faster Payments for urgency |
| Payment rejection | Insufficient funds | Implement retry mechanism with notification |
| Confirmation of Payee mismatch | Name doesn't match account | Verify account details with recipient |
| BACS return | Various return reasons | Implement proper handling for each return code |

## Future Developments

- **New Payments Architecture (NPA)** - Modernization of UK payment infrastructure
- **Enhanced data capabilities** - Support for more remittance information
- **Request to Pay** - New flexible payment request service
- **Confirmation of Payee enhancements** - Expanded coverage and functionality
- **Cross-border improvements** - Enhanced international payment capabilities
- **ISO 20022 migration** - Standardization of payment messaging

## References

1. Pay.UK - [Faster Payments](https://www.fasterpayments.org.uk/)
2. Pay.UK - [BACS](https://www.bacs.co.uk/)
3. Bank of England - [CHAPS](https://www.bankofengland.co.uk/payment-and-settlement/chaps)
4. Financial Conduct Authority - [Payment Services Regulations](https://www.fca.org.uk/firms/payment-services-regulations)
5. UK Finance - [Payment Standards](https://www.ukfinance.org.uk/policy-and-guidance/payments)
