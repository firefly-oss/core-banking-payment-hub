# ACH Payments

## Overview

The Automated Clearing House (ACH) Network is a batch processing system that facilitates electronic funds transfers between financial institutions in the United States. The Firefly Core Banking Payment Hub provides comprehensive support for ACH payment types, ensuring compliance with NACHA (National Automated Clearing House Association) rules and regulations.

## ACH Payment Types

### ACH Credit

ACH Credit transactions are "push" payments where the originator initiates the transfer of funds to a receiver's account.

**Key Features:**
- Funds are pushed from the originator to the receiver
- Typically used for direct deposit, vendor payments, and B2B payments
- Settlement typically occurs within 1-2 business days
- Lower cost alternative to wire transfers
- Supports both one-time and recurring payments

**Technical Implementation:**
- NACHA file format with batch header and entry detail records
- Standard Entry Class (SEC) codes define the type of transaction
- Addenda records for additional payment information
- Return code handling for failed transactions

### ACH Debit

ACH Debit transactions are "pull" payments where the originator, with prior authorization, withdraws funds from a receiver's account.

**Key Features:**
- Funds are pulled from the receiver's account by the originator
- Typically used for bill payments, subscriptions, and recurring charges
- Requires prior authorization from the account holder
- Settlement typically occurs within 1-2 business days
- Higher risk of returns compared to ACH Credits

**Technical Implementation:**
- NACHA file format with specific transaction codes for debits
- Authorization tracking and management
- Return and notification of change handling
- Risk management for unauthorized returns

### Wire Transfer

While not technically part of the ACH network, wire transfers are included in this documentation as they are another important US payment method.

**Key Features:**
- Real-time, irrevocable transfers between US banks
- Typically used for high-value, time-sensitive payments
- Same-day settlement
- Higher fees compared to ACH transfers
- Fedwire or CHIPS network processing

**Technical Implementation:**
- Fedwire message format
- Real-time processing
- Immediate finality
- Higher security requirements

## ACH Payment Flows

### ACH Credit Flow

1. **Origination**: The originator initiates an ACH credit through their financial institution (ODFI - Originating Depository Financial Institution)
2. **Batch Processing**: The ODFI batches ACH transactions and submits them to the ACH Operator (Federal Reserve or The Clearing House)
3. **Distribution**: The ACH Operator sorts the transactions and sends them to the appropriate receiving financial institutions (RDFI - Receiving Depository Financial Institution)
4. **Settlement**: Funds are settled between the ODFI and RDFI
5. **Crediting**: The RDFI credits the receiver's account
6. **Reporting**: Transaction reports are generated for reconciliation

### ACH Debit Flow

1. **Authorization**: The receiver authorizes the originator to debit their account
2. **Origination**: The originator initiates an ACH debit through their ODFI
3. **Batch Processing**: The ODFI batches ACH transactions and submits them to the ACH Operator
4. **Distribution**: The ACH Operator sorts the transactions and sends them to the appropriate RDFIs
5. **Verification**: The RDFI verifies the transaction and checks for sufficient funds
6. **Debiting**: The RDFI debits the receiver's account
7. **Settlement**: Funds are settled between the ODFI and RDFI
8. **Reporting**: Transaction reports are generated for reconciliation

### Wire Transfer Flow

1. **Initiation**: The sender initiates a wire transfer through their bank
2. **Processing**: The sending bank processes the wire transfer in real-time
3. **Network Transmission**: The transfer is sent through Fedwire or CHIPS
4. **Receiving**: The receiving bank accepts the wire transfer
5. **Crediting**: The receiving bank immediately credits the beneficiary's account
6. **Confirmation**: Confirmation is sent back to the sending bank

## ACH Data Requirements

### Mandatory Fields for ACH Transactions

- Originator name
- Originator account number and type
- Originating financial institution routing number
- Receiver name
- Receiver account number and type
- Receiving financial institution routing number (ABA routing number)
- Transaction amount
- Standard Entry Class (SEC) code
- Effective entry date
- Trace number
- Company Entry Description

### ABA Routing Numbers

ACH payments rely on ABA (American Bankers Association) routing numbers for identifying financial institutions:

- 9-digit number
- First 4 digits: Federal Reserve routing symbol
- Next 4 digits: ABA institution identifier
- Last digit: Check digit

Example: `021000021` (JPMorgan Chase Bank)

### Standard Entry Class (SEC) Codes

SEC codes identify the type of ACH transaction:

- **PPD** (Prearranged Payment and Deposit) - Consumer payments
- **CCD** (Corporate Credit or Debit) - Business payments
- **WEB** (Internet-Initiated Entry) - Online payments
- **TEL** (Telephone-Initiated Entry) - Phone payments
- **IAT** (International ACH Transaction) - Cross-border payments
- **ARC** (Accounts Receivable Entry) - Check conversion
- **POP** (Point-of-Purchase Entry) - In-person check conversion
- **BOC** (Back Office Conversion) - Check conversion
- **RCK** (Re-presented Check Entry) - Returned check collection

## ACH Geographic Scope

ACH is primarily a US payment system, but it has expanded to include:

- **Domestic US payments** - Primary use case
- **US territories** - Including Puerto Rico, Guam, US Virgin Islands
- **International ACH Transactions (IAT)** - Cross-border payments with specific requirements

## Regulatory Compliance

### NACHA Rules

The National Automated Clearing House Association (NACHA) governs the ACH Network with specific rules:

- Authorization requirements
- Timing requirements
- Format specifications
- Return timeframes
- Error resolution procedures
- Risk management
- Data security requirements

### Regulation E

For consumer ACH transactions, Regulation E (Electronic Fund Transfer Act) provides consumer protections:

- Disclosure requirements
- Error resolution procedures
- Unauthorized transaction liability limits
- Receipt requirements
- Statement requirements

### Regulation CC

Regulation CC (Expedited Funds Availability Act) governs funds availability:

- Next-day availability for certain electronic payments
- Availability schedule for deposited funds
- Hold notification requirements
- Expedited recredit procedures

### BSA/AML Requirements

Bank Secrecy Act (BSA) and Anti-Money Laundering (AML) requirements include:

- Customer identification
- Transaction monitoring
- Suspicious activity reporting
- Record keeping
- Risk assessment

## Implementation in Firefly Core Banking Payment Hub

### AchPaymentController

The `AchPaymentController` exposes RESTful endpoints for ACH payment operations:

- `POST /api/v1/payments/ach/simulate` - Simulates an ACH payment
- `POST /api/v1/payments/ach/execute` - Executes an ACH payment
- `POST /api/v1/payments/ach/cancel` - Cancels an ACH payment
- `POST /api/v1/payments/ach/schedule` - Schedules an ACH payment

### AchPaymentService

The `AchPaymentService` provides the business logic for ACH payment operations:

- Validation of ACH payment requests
- Routing to appropriate ACH payment providers
- Handling of authorization requirements
- Processing of payment responses
- Return code handling

### AchPaymentProvider

The `AchPaymentProvider` interface defines the contract for ACH payment processing:

- `simulate(AchTransferRequestDTO)` - Simulates an ACH payment
- `execute(AchTransferRequestDTO)` - Executes an ACH payment
- `cancel(String, String)` - Cancels an ACH payment
- `schedule(AchTransferRequestDTO, String)` - Schedules an ACH payment

## ACH Processing Schedule

The ACH Network operates on a specific processing schedule:

- **Standard ACH**: 1-2 business days settlement
- **Same Day ACH**: Multiple settlement windows throughout the business day
  - Morning submission deadline (typically 10:30 AM ET)
  - Afternoon submission deadline (typically 2:45 PM ET)
  - Late afternoon submission deadline (typically 4:45 PM ET)
- **Weekend/Holiday Processing**: No processing on weekends or Federal Reserve holidays

## ACH Transaction Limits

ACH transactions are subject to certain limits:

- **Standard ACH**: No dollar limit
- **Same Day ACH**: $1,000,000 per transaction (as of March 2022)
- **IAT**: Subject to OFAC and BSA/AML limits
- **Financial Institution Limits**: Individual banks may impose their own limits

## Example API Requests

### ACH Credit Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/ach/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12348",
    "paymentType": "ACH_CREDIT",
    "amount": 500.00,
    "currency": "USD",
    "originator": {
      "name": "John Doe",
      "address": "123 Main St, New York, NY",
      "countryCode": "US"
    },
    "originatorAccount": {
      "accountNumber": "12345678",
      "accountType": "CHECKING"
    },
    "receiver": {
      "name": "Jane Smith",
      "address": "456 Oak St, Los Angeles, CA",
      "countryCode": "US"
    },
    "receiverAccount": {
      "accountNumber": "87654321",
      "accountType": "SAVINGS"
    },
    "receivingBankRoutingNumber": "021000021",
    "remittanceInformation": "Invoice payment #54321",
    "secCode": "PPD"
  }'
```

### ACH Debit Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/ach/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12349",
    "paymentType": "ACH_DEBIT",
    "amount": 100.00,
    "currency": "USD",
    "originator": {
      "name": "Subscription Service Inc.",
      "address": "789 Business Ave, Chicago, IL",
      "countryCode": "US"
    },
    "originatorAccount": {
      "accountNumber": "98765432",
      "accountType": "CHECKING"
    },
    "receiver": {
      "name": "John Doe",
      "address": "123 Main St, New York, NY",
      "countryCode": "US"
    },
    "receiverAccount": {
      "accountNumber": "12345678",
      "accountType": "CHECKING"
    },
    "receivingBankRoutingNumber": "021000021",
    "remittanceInformation": "Monthly subscription",
    "secCode": "WEB",
    "authorizationDate": "2023-01-15"
  }'
```

## NACHA File Format

ACH transactions are typically processed in batch files using the NACHA format:

### File Structure

1. **File Header Record** - Contains information about the file originator
2. **Batch Header Record** - Contains information about the batch originator
3. **Entry Detail Record** - Contains information about each transaction
4. **Addenda Record(s)** - Contains additional information about the transaction (optional)
5. **Batch Control Record** - Contains batch totals and counts
6. **File Control Record** - Contains file totals and counts

### Example NACHA Format

```
101 021000021 2100002123060101A094101Bank of America           ACME Corp.                   
5200ACME Corp.                        1234567890PPDPAYROLL         230601230601   1021000020000001
62212345678         0000050000               JOHN DOE                      0021000020000001
6221987654321       0000075000               JANE SMITH                    0021000020000002
822000000200210000210000000125000000000000001234567890                         021000020000001
9000001000001000000020021000021000000012500000000000000                                       
```

## ACH Return Codes

ACH transactions can be returned for various reasons, each with a specific return code:

- **R01**: Insufficient funds
- **R02**: Account closed
- **R03**: No account/Unable to locate account
- **R04**: Invalid account number
- **R05**: Unauthorized debit to consumer account
- **R06**: Returned per ODFI's request
- **R07**: Authorization revoked by customer
- **R08**: Payment stopped
- **R09**: Uncollected funds
- **R10**: Customer advises not authorized
- **R11**: Check truncation entry return
- **R16**: Account frozen
- **R20**: Non-transaction account
- **R23**: Credit entry refused by receiver
- **R24**: Duplicate entry
- **R29**: Corporate customer advises not authorized

## Best Practices

1. **Validate routing numbers** using the check digit algorithm
2. **Include detailed remittance information** for better reconciliation
3. **Implement proper error handling** for ACH-specific return codes
4. **Consider Same Day ACH** for time-sensitive payments
5. **Maintain proper authorization records** for debits
6. **Monitor return rates** to stay within NACHA thresholds
7. **Implement risk management** for high-risk transactions
8. **Consider transaction timing** based on ACH processing windows
9. **Properly format receiver information** to avoid returns
10. **Implement proper notification procedures** for failed transactions

## Common Issues and Troubleshooting

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| R01 - Insufficient funds | Receiver doesn't have enough money | Retry the transaction after a few days |
| R02 - Account closed | Receiver's account is no longer active | Contact receiver for updated account information |
| R03 - No account | Invalid account number | Verify account number with receiver |
| R05 - Unauthorized debit | Receiver disputes authorization | Verify authorization and provide proof if challenged |
| R10 - Customer advises not authorized | Consumer claims no authorization | Review authorization records and process |
| R20 - Non-transaction account | Account doesn't support ACH | Verify account type with receiver |

## Future Developments

- **Expanded Same Day ACH** - Additional processing windows and higher limits
- **Enhanced data capabilities** - Support for more remittance information
- **Real-time payments** - Integration with the RTP Network and FedNow
- **Enhanced security** - Additional fraud prevention measures
- **API standardization** - Industry-standard APIs for ACH processing
- **Cross-border enhancements** - Improved international ACH capabilities

## References

1. NACHA - [ACH Rules](https://www.nacha.org/rules)
2. Federal Reserve - [ACH Services](https://www.frbservices.org/financial-services/ach)
3. Consumer Financial Protection Bureau - [Regulation E](https://www.consumerfinance.gov/rules-policy/regulations/1005/)
4. Federal Reserve - [Regulation CC](https://www.federalreserve.gov/supervisionreg/regcc.htm)
5. ABA - [Routing Number Policy](https://www.aba.com/advocacy/policy-analysis/routing-number-policy)
