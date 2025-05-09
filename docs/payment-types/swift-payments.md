# SWIFT Payments

## Overview

SWIFT (Society for Worldwide Interbank Financial Telecommunication) provides a network that enables financial institutions worldwide to send and receive information about financial transactions in a secure, standardized, and reliable environment. The Firefly Core Banking Payment Hub offers comprehensive support for SWIFT payment messages, enabling international transfers across the global banking network.

## SWIFT Message Types

### MT103 - Single Customer Credit Transfer

The MT103 is the most commonly used message type for customer credit transfers. It is used when a customer transfers funds to another customer's account at another financial institution.

**Key Features:**
- Customer-to-customer payment
- Support for cross-border and domestic payments
- Detailed payment information including remittance data
- Support for regulatory reporting requirements
- Can be used for both high-value and low-value payments

**Technical Implementation:**
- Structured format with mandatory and optional fields
- Support for various character sets
- Validation rules for each field
- Sequence validation for proper message structure

### MT202 - General Financial Institution Transfer

The MT202 is used for financial institution-to-financial institution transfers, such as cover payments, settlements, or treasury operations.

**Key Features:**
- Bank-to-bank transfers
- Typically higher value than MT103
- Used for correspondent banking relationships
- Simpler structure than MT103 (less customer information)
- Used for liquidity management and settlement

**Technical Implementation:**
- Structured format with specific field requirements
- Support for nostro/vostro account references
- Integration with bank treasury systems
- High-priority processing

### PACS.008 - FIToFICustomerCreditTransfer (ISO 20022)

PACS.008 is the ISO 20022 equivalent of the MT103, used for customer credit transfers in the new message standard that is gradually replacing the MT messages.

**Key Features:**
- XML-based format with rich structured data
- Enhanced character set support
- More detailed payment information
- Better support for regulatory requirements
- Designed for straight-through processing

**Technical Implementation:**
- XML schema validation
- Support for extended remittance information
- Enhanced party information
- Regulatory reporting elements
- Support for purpose codes

## SWIFT Payment Flows

### Correspondent Banking Model

Most SWIFT payments follow the correspondent banking model, where banks maintain relationships (nostro/vostro accounts) with other banks to facilitate international transfers.

1. **Originating Bank**: The sender's bank initiates the payment
2. **Correspondent Bank(s)**: One or more intermediary banks may be involved
3. **Beneficiary Bank**: The receiver's bank that credits the final beneficiary

### Direct SWIFT Payment Flow

1. **Payment Initiation**: Customer initiates payment at their bank
2. **SWIFT Message Creation**: Bank creates MT103/PACS.008 message
3. **SWIFT Network Transmission**: Message is sent through SWIFT network
4. **Beneficiary Bank Processing**: Receiving bank processes the payment
5. **Account Crediting**: Funds are credited to the beneficiary's account
6. **Confirmation**: Optional confirmation message back to the originating bank

### Cover Payment Flow (MT103 + MT202)

For payments that require liquidity between correspondent banks:

1. **MT103**: Sent from originating bank to beneficiary bank with payment details
2. **MT202**: Sent from originating bank to correspondent bank for settlement
3. **Settlement**: Correspondent bank settles with beneficiary bank
4. **Crediting**: Beneficiary bank credits the final customer

## SWIFT Data Requirements

### Mandatory Fields for MT103

- Sender's Reference (Field 20)
- Bank Operation Code (Field 23B)
- Value Date/Currency/Amount (Field 32A)
- Ordering Customer (Field 50K or 50A)
- Beneficiary Customer (Field 59)
- Details of Charges (Field 71A)

### Mandatory Fields for MT202

- Sender's Reference (Field 20)
- Related Reference (Field 21)
- Value Date/Currency/Amount (Field 32A)
- Receiver's Correspondent (Field 54A, optional)
- Beneficiary Institution (Field 58A)

### Mandatory Fields for PACS.008

- Message Identification
- Creation Date and Time
- Settlement Amount and Currency
- Debtor Information
- Debtor Agent (Debtor's Bank)
- Creditor Agent (Creditor's Bank)
- Creditor Information

### BIC and Routing Codes

SWIFT payments rely on BIC (Bank Identifier Code, also known as SWIFT code) for routing:

- 8 or 11 characters long
- First 4 characters: Bank code
- Next 2 characters: Country code
- Next 2 characters: Location code
- Last 3 characters: Branch code (optional)

Example: `DEUTDEFF` (Deutsche Bank, Frankfurt, Germany)

## SWIFT Geographic Scope

SWIFT connects more than 11,000 financial institutions in over 200 countries and territories, making it the most extensive network for international payments.

### Regional Considerations

Different regions may have specific requirements for SWIFT payments:

- **Europe**: Integration with SEPA for euro payments
- **USA**: OFAC compliance and ABA routing numbers
- **Asia**: Local language requirements and specific regulatory reporting
- **Middle East**: Compliance with sanctions and enhanced due diligence
- **Africa**: Correspondent banking relationships and currency controls

## Regulatory Compliance

### Sanctions Screening

All SWIFT payments must be screened against various sanctions lists:

- US OFAC (Office of Foreign Assets Control)
- UN Security Council
- EU sanctions
- UK sanctions
- Local country sanctions

### AML/CTF Requirements

Anti-Money Laundering and Counter-Terrorism Financing requirements include:

- Customer Due Diligence (CDD)
- Enhanced Due Diligence (EDD) for high-risk payments
- Transaction monitoring
- Suspicious activity reporting
- Record keeping (typically 5-7 years)

### FATF Recommendations

The Financial Action Task Force (FATF) provides recommendations that affect SWIFT payments:

- Travel Rule (originator and beneficiary information)
- Risk-based approach to payment screening
- Transparency requirements
- Correspondent banking due diligence

## Implementation in Firefly Core Banking Payment Hub

### SwiftPaymentController

The `SwiftPaymentController` exposes RESTful endpoints for SWIFT payment operations:

- `POST /api/v1/payments/swift/simulate` - Simulates a SWIFT payment
- `POST /api/v1/payments/swift/execute` - Executes a SWIFT payment
- `POST /api/v1/payments/swift/cancel` - Cancels a SWIFT payment
- `POST /api/v1/payments/swift/schedule` - Schedules a SWIFT payment

### SwiftPaymentService

The `SwiftPaymentService` provides the business logic for SWIFT payment operations:

- Validation of SWIFT payment requests
- Routing to appropriate SWIFT payment providers
- Handling of SCA requirements for high-value payments
- Processing of payment responses
- Sanctions screening integration

### SwiftPaymentProvider

The `SwiftPaymentProvider` interface defines the contract for SWIFT payment processing:

- `simulate(SwiftPaymentRequestDTO)` - Simulates a SWIFT payment
- `execute(SwiftPaymentRequestDTO)` - Executes a SWIFT payment
- `cancel(String, String)` - Cancels a SWIFT payment
- `schedule(SwiftPaymentRequestDTO, String)` - Schedules a SWIFT payment

## Example API Requests

### SWIFT MT103 Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/swift/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12346",
    "paymentType": "SWIFT_MT103",
    "amount": 1000.00,
    "currency": "USD",
    "orderingCustomer": {
      "name": "John Doe",
      "address": "123 Main St",
      "countryCode": "US"
    },
    "orderingInstitution": {
      "bic": "DEUTDEFF",
      "bankName": "Deutsche Bank"
    },
    "beneficiaryCustomer": {
      "name": "Jane Smith",
      "address": "456 Oak St",
      "countryCode": "GB"
    },
    "beneficiaryInstitution": {
      "bic": "CHASUS33",
      "bankName": "JPMorgan Chase",
      "bankCountryCode": "US"
    },
    "paymentDetails": "International payment",
    "chargeBearer": "SHARED",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

### SWIFT MT202 Payment Execution

```bash
curl -X POST http://localhost:8080/api/v1/payments/swift/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12347",
    "paymentType": "SWIFT_MT202",
    "amount": 5000000.00,
    "currency": "EUR",
    "orderingInstitution": {
      "bic": "DEUTDEFF",
      "bankName": "Deutsche Bank"
    },
    "beneficiaryInstitution": {
      "bic": "BARCGB22",
      "bankName": "Barclays Bank",
      "bankCountryCode": "GB"
    },
    "relatedReference": "RELATED-REF-12345",
    "paymentDetails": "Interbank settlement",
    "chargeBearer": "SHARED"
  }'
```

## SWIFT Message Formats

### MT103 Format Example

```
{1:F01BANKBEBBAXXX0000000000}{2:I103BANKDEFFXXXXN}{4:
:20:REFERENCE
:23B:CRED
:32A:230601EUR1000,00
:50K:/12345678
JOHN DOE
123 MAIN ST
:53A:DEUTDEFF
:57A:CHASUS33
:59:/87654321
JANE SMITH
456 OAK ST
:70:INVOICE PAYMENT
:71A:SHA
-}
```

### PACS.008 Format Example (XML)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>MSGID-12345</MsgId>
      <CreDtTm>2023-06-01T10:30:00Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <SttlmInf>
        <SttlmMtd>INDA</SttlmMtd>
      </SttlmInf>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>INSTRID-12345</InstrId>
        <EndToEndId>E2EID-12345</EndToEndId>
        <TxId>TXID-12345</TxId>
      </PmtId>
      <PmtTpInf>
        <InstrPrty>NORM</InstrPrty>
      </PmtTpInf>
      <IntrBkSttlmAmt Ccy="USD">1000.00</IntrBkSttlmAmt>
      <IntrBkSttlmDt>2023-06-01</IntrBkSttlmDt>
      <ChrgBr>SHAR</ChrgBr>
      <Dbtr>
        <Nm>John Doe</Nm>
        <PstlAdr>
          <StrtNm>Main St</StrtNm>
          <BldgNb>123</BldgNb>
          <Ctry>US</Ctry>
        </PstlAdr>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>12345678</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      <DbtrAgt>
        <FinInstnId>
          <BICFI>DEUTDEFF</BICFI>
        </FinInstnId>
      </DbtrAgt>
      <CdtrAgt>
        <FinInstnId>
          <BICFI>CHASUS33</BICFI>
        </FinInstnId>
      </CdtrAgt>
      <Cdtr>
        <Nm>Jane Smith</Nm>
        <PstlAdr>
          <StrtNm>Oak St</StrtNm>
          <BldgNb>456</BldgNb>
          <Ctry>GB</Ctry>
        </PstlAdr>
      </Cdtr>
      <CdtrAcct>
        <Id>
          <Othr>
            <Id>87654321</Id>
          </Othr>
        </Id>
      </CdtrAcct>
      <RmtInf>
        <Ustrd>Invoice payment</Ustrd>
      </RmtInf>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

## SWIFT Fees and Charges

SWIFT payments typically involve several types of fees:

### Charge Types

- **OUR**: All charges paid by the sender
- **BEN**: All charges paid by the beneficiary
- **SHA**: Shared charges (sender pays sending charges, beneficiary pays receiving charges)

Note: For payments within the EEA (European Economic Area), only SHA is permitted under PSD2.

### Fee Components

1. **Sending Bank Fee**: Charged by the originating bank
2. **Correspondent Bank Fee(s)**: Charged by intermediary banks
3. **Receiving Bank Fee**: Charged by the beneficiary bank
4. **SWIFT Network Fee**: Charged for using the SWIFT network
5. **Foreign Exchange Margin**: Applied when currency conversion is needed

## Processing Times

SWIFT payments typically follow these processing timeframes:

- **Standard Processing**: 1-3 business days
- **Priority/Urgent**: Same day or next day
- **Cut-off Times**: Vary by bank and currency
- **Value Dating**: Usually same day or next day

Factors affecting processing time:
- Time zones
- Bank working hours
- Currency
- Correspondent banking relationships
- Compliance checks

## Best Practices

1. **Include complete beneficiary information** to avoid payment delays
2. **Use correct BIC codes** and verify them before submission
3. **Consider cut-off times** when scheduling payments
4. **Include purpose of payment** for regulatory compliance
5. **Be aware of country-specific requirements** for certain destinations
6. **Monitor payment status** through tracking services
7. **Implement proper error handling** for SWIFT-specific error codes
8. **Consider FX implications** for cross-currency payments

## Common Issues and Troubleshooting

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| Payment delay | Compliance checks | Provide additional information if requested |
| Incorrect BIC | Formatting error or outdated information | Verify BIC using SWIFT directory |
| Rejected payment | Sanctions hit | Review payment details and provide justification if legitimate |
| Missing information | Incomplete payment details | Ensure all mandatory fields are completed |
| Unexpected fees | Intermediary banks | Use OUR charge type if sender must pay all fees |
| FX rate issues | Market fluctuations | Consider using forward contracts for large payments |

## Future Developments

- **SWIFT gpi**: Enhanced payment tracking and transparency
- **ISO 20022 Migration**: Complete transition from MT to MX messages by 2025
- **API-based Access**: Direct API access to SWIFT services
- **Enhanced Compliance**: More sophisticated screening and monitoring
- **Blockchain Integration**: Potential integration with distributed ledger technologies
- **CBDC Support**: Preparation for Central Bank Digital Currencies

## References

1. SWIFT - [MT Standards](https://www.swift.com/standards/mt-standards)
2. SWIFT - [ISO 20022 Migration](https://www.swift.com/standards/iso-20022-migration)
3. SWIFT - [SWIFT gpi](https://www.swift.com/our-solutions/swift-gpi)
4. BIS - [Correspondent Banking](https://www.bis.org/cpmi/publ/d147.pdf)
5. FATF - [Recommendations](https://www.fatf-gafi.org/publications/fatfrecommendations/)
