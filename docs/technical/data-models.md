# Data Models

## Overview

The Firefly Core Banking Payment Hub uses a comprehensive set of data models to represent payment-related information across different payment types and operations. This document provides a detailed explanation of the key data models, their relationships, and their usage within the system.

The data models are designed to balance several requirements:
- Support for diverse payment types with different data needs
- Consistency across similar payment operations
- Compliance with industry standards and regulations
- Extensibility for future payment types
- Efficient data transfer and processing

## Core Data Model Principles

The data models in the payment hub follow these core principles:

### Inheritance Hierarchy

Data models use inheritance to share common attributes while allowing specialization for specific payment types:

```
BasePaymentRequestDTO
├── SepaPaymentRequestDTO
├── SwiftPaymentRequestDTO
├── AchTransferRequestDTO
├── UkPaymentRequestDTO
└── InternalTransferRequestDTO
```

This approach reduces duplication and ensures consistency while allowing for type-specific attributes.

### Builder Pattern

Data models use the Builder pattern (via Lombok's `@Builder` annotation) to facilitate the creation of complex objects:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasePaymentRequestDTO {
    // Fields and methods...
}
```

The `@SuperBuilder` annotation ensures that the builder pattern works correctly with inheritance.

### Validation Annotations

Data models include validation annotations to enforce data integrity:

```java
@NotBlank(message = "Request ID is required")
private String requestId;

@NotNull(message = "Amount is required")
@Positive(message = "Amount must be positive")
private BigDecimal amount;

@NotBlank(message = "Currency is required")
@Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
private String currency;
```

These annotations are processed by the validation framework to ensure that data meets the required constraints.

### Immutability

Data models are designed to be immutable where appropriate, using Lombok's `@Value` annotation for value objects:

```java
@Value
public class AccountIdentifier {
    String accountNumber;
    String sortCode;
}
```

Immutability helps prevent bugs related to unexpected state changes and improves thread safety.

## Key Data Models

### Request DTOs

Request DTOs represent the input data for payment operations:

#### BasePaymentRequestDTO

The base class for all payment requests:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasePaymentRequestDTO {
    @NotBlank(message = "Request ID is required")
    private String requestId;
    
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String currency;
    
    private ScaDTO sca;
}
```

#### SepaPaymentRequestDTO

Request DTO for SEPA payments:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SepaPaymentRequestDTO extends BasePaymentRequestDTO {
    @Valid
    @NotNull(message = "Debtor account is required")
    private SepaAccountDTO debtorAccount;
    
    @Valid
    @NotNull(message = "Creditor account is required")
    private SepaAccountDTO creditorAccount;
    
    @NotBlank(message = "Remittance information is required")
    @Size(max = 140, message = "Remittance information must not exceed 140 characters")
    private String remittanceInformation;
    
    private String endToEndId;
    private String chargeBearer;
    private LocalDate requestedExecutionDate;
}
```

#### SwiftPaymentRequestDTO

Request DTO for SWIFT payments:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwiftPaymentRequestDTO extends BasePaymentRequestDTO {
    @Valid
    @NotNull(message = "Ordering customer is required")
    private PartyInfoDTO orderingCustomer;
    
    @Valid
    private FinancialInstitutionDTO orderingInstitution;
    
    @Valid
    @NotNull(message = "Beneficiary customer is required")
    private PartyInfoDTO beneficiaryCustomer;
    
    @Valid
    @NotNull(message = "Beneficiary institution is required")
    private FinancialInstitutionDTO beneficiaryInstitution;
    
    @NotBlank(message = "Payment details are required")
    private String paymentDetails;
    
    private String chargeBearer;
    private String instructionCode;
    private String regulatoryReporting;
    private String relatedReference;
}
```

#### AchTransferRequestDTO

Request DTO for ACH transfers:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AchTransferRequestDTO extends BasePaymentRequestDTO {
    @Valid
    @NotNull(message = "Originator information is required")
    private PartyInfoDTO originator;
    
    @Valid
    @NotNull(message = "Originator account information is required")
    private AccountInfoDTO originatorAccount;
    
    @Valid
    @NotNull(message = "Receiver information is required")
    private PartyInfoDTO receiver;
    
    @Valid
    @NotNull(message = "Receiver account information is required")
    private AccountInfoDTO receiverAccount;
    
    @NotBlank(message = "ABA routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "ABA routing number must be 9 digits")
    private String receivingBankRoutingNumber;
    
    @NotBlank(message = "Remittance information is required")
    private String remittanceInformation;
    
    private String secCode;
    private String traceNumber;
    private Boolean addendaRecordIndicator;
    private String addendaInformation;
}
```

#### UkPaymentRequestDTO

Base request DTO for UK payments:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UkPaymentRequestDTO extends BasePaymentRequestDTO {
    @NotBlank(message = "Debtor name is required")
    private String debtorName;
    
    @Valid
    @NotNull(message = "Debtor account is required")
    private UkAccountDTO debtorAccount;
    
    @NotBlank(message = "Creditor name is required")
    private String creditorName;
    
    @Valid
    @NotNull(message = "Creditor account is required")
    private UkAccountDTO creditorAccount;
    
    @NotBlank(message = "Reference is required")
    @Size(max = 18, message = "Reference must not exceed 18 characters")
    private String reference;
}
```

#### InternalTransferRequestDTO

Request DTO for internal transfers:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InternalTransferRequestDTO extends BasePaymentRequestDTO {
    @NotBlank(message = "Source account ID is required")
    private String sourceAccountId;
    
    @NotBlank(message = "Destination account ID is required")
    private String destinationAccountId;
    
    @NotBlank(message = "Reference is required")
    private String reference;
    
    private String transferType;
    private LocalDate executionDate;
}
```

### Result DTOs

Result DTOs represent the output data from payment operations:

#### PaymentSimulationResultDTO

Result of a payment simulation operation:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSimulationResultDTO {
    private String requestId;
    private PaymentType paymentType;
    private PaymentOperationType operationType;
    private PaymentProviderType provider;
    private LocalDateTime timestamp;
    
    private boolean success;
    private String status;
    private String rejectReason;
    private String providerReference;
    
    private BigDecimal amount;
    private String currency;
    private BigDecimal estimatedFee;
    private String feeType;
    
    private boolean scaRequired;
    private boolean scaCompleted;
    private ScaResultDTO scaResult;
    
    private Map<String, String> additionalInfo;
}
```

#### PaymentExecutionResultDTO

Result of a payment execution operation:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecutionResultDTO {
    private String paymentId;
    private String requestId;
    private PaymentType paymentType;
    private PaymentOperationType operationType;
    private PaymentProviderType provider;
    private LocalDateTime timestamp;
    
    private boolean success;
    private PaymentStatus status;
    private String rejectReason;
    private String providerReference;
    
    private BigDecimal amount;
    private String currency;
    private BigDecimal fee;
    private String feeType;
    
    private LocalDate executionDate;
    private LocalDate expectedSettlementDate;
    private String transactionReference;
    private String clearingSystemReference;
    
    private LocalDateTime receivedTimestamp;
    private boolean requiresAuthorization;
    
    private boolean scaRequired;
    private boolean scaCompleted;
    private ScaResultDTO scaResult;
    
    private Map<String, String> additionalInfo;
}
```

#### PaymentScheduleResultDTO

Result of a payment scheduling operation:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentScheduleResultDTO {
    private String paymentId;
    private String requestId;
    private PaymentType paymentType;
    private PaymentOperationType operationType;
    private PaymentProviderType provider;
    private LocalDateTime timestamp;
    
    private boolean success;
    private PaymentStatus status;
    private String rejectReason;
    private String providerReference;
    
    private LocalDate scheduledExecutionDate;
    private LocalDate expectedSettlementDate;
    private String transactionReference;
    private String recurrencePattern;
    private LocalDate recurrenceEndDate;
    
    private boolean requiresAuthorization;
    
    private boolean scaRequired;
    private boolean scaCompleted;
    private ScaResultDTO scaResult;
    
    private Map<String, String> additionalInfo;
}
```

#### PaymentCancellationResultDTO

Result of a payment cancellation operation:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancellationResultDTO {
    private String paymentId;
    private PaymentType paymentType;
    private PaymentOperationType operationType;
    private PaymentProviderType provider;
    private LocalDateTime timestamp;
    
    private boolean success;
    private String status;
    private String rejectReason;
    private String cancellationReference;
    
    private boolean scaRequired;
    private boolean scaCompleted;
    private ScaResultDTO scaResult;
    
    private Map<String, String> additionalInfo;
}
```

### Common DTOs

Common DTOs used across different payment types:

#### AccountInfoDTO

Represents account information:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoDTO {
    private String accountNumber;
    private String accountType;
    private String accountName;
    private String currency;
}
```

#### SepaAccountDTO

Represents a SEPA account:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepaAccountDTO {
    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
    private String iban;
    
    private String bic;
    private String accountName;
}
```

#### UkAccountDTO

Represents a UK account:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UkAccountDTO {
    @NotBlank(message = "Sort code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Sort code must be 6 digits")
    private String sortCode;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^\\d{8}$", message = "Account number must be 8 digits")
    private String accountNumber;
    
    private String accountName;
}
```

#### PartyInfoDTO

Represents information about a party involved in a payment:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyInfoDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String address;
    private String city;
    private String postalCode;
    private String stateOrProvince;
    
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be a 2-letter ISO code")
    private String countryCode;
    
    private String phoneNumber;
    private String emailAddress;
    private String identificationType;
    private String identificationValue;
}
```

#### FinancialInstitutionDTO

Represents information about a financial institution:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInstitutionDTO {
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Invalid BIC format")
    private String bic;
    
    private String bankName;
    private String bankAddress;
    private String bankCity;
    private String bankPostalCode;
    
    @Pattern(regexp = "^[A-Z]{2}$", message = "Bank country code must be a 2-letter ISO code")
    private String bankCountryCode;
}
```

#### ScaDTO

Represents Strong Customer Authentication information:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScaDTO {
    @NotBlank(message = "SCA method is required")
    private String method;
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
    
    @NotBlank(message = "Authentication code is required")
    private String authenticationCode;
}
```

### Enumerations

Enumerations define the fixed sets of values used in the system:

#### PaymentType

Defines the types of payments supported by the system:

```java
public enum PaymentType {
    // SEPA payment types
    SEPA_SCT("SEPA Credit Transfer"),
    SEPA_ICT("SEPA Instant Credit Transfer"),
    SEPA_SDD("SEPA Direct Debit"),
    SEPA_SDD_CORE("SEPA Direct Debit Core"),
    SEPA_SDD_B2B("SEPA Direct Debit Business-to-Business"),
    // ... other SEPA payment types
    
    // SWIFT payment types
    SWIFT_MT103("SWIFT MT103 Customer Credit Transfer"),
    SWIFT_MT202("SWIFT MT202 Financial Institution Transfer"),
    SWIFT_PACS_008("SWIFT MX PACS.008 Customer Credit Transfer"),
    
    // ACH payment types
    ACH_CREDIT("ACH Credit Transfer"),
    ACH_DEBIT("ACH Debit Transfer"),
    WIRE_TRANSFER("US Wire Transfer"),
    
    // UK payment types
    UK_FPS("UK Faster Payments Service"),
    UK_BACS("UK BACS Direct Credit"),
    UK_BACS_DIRECT_DEBIT("UK BACS Direct Debit"),
    UK_CHAPS("UK CHAPS Payment"),
    UK_STANDING_ORDER("UK Standing Order"),
    
    // European payment types
    TARGET2("TARGET2 Payment"),
    TIPS("TIPS Instant Payment"),
    EBA_STEP2("EBA STEP2 Payment"),
    
    // Internal payment types
    INTERNAL_TRANSFER("Internal Transfer"),
    INTERNAL_BULK_TRANSFER("Internal Bulk Transfer"),
    INTERNAL_STANDING_ORDER("Internal Standing Order"),
    INTERNAL_FUTURE_DATED("Internal Future Dated Transfer");
    
    private final String description;
    
    PaymentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

#### PaymentProviderType

Defines the types of payment providers:

```java
public enum PaymentProviderType {
    SEPA_PROVIDER("SEPA payment provider"),
    SWIFT_PROVIDER("SWIFT payment provider"),
    ACH_PROVIDER("ACH payment provider for US transfers"),
    UK_PROVIDER("UK payment provider for FPS, BACS, and CHAPS"),
    TARGET2_PROVIDER("TARGET2 payment provider"),
    TIPS_PROVIDER("TIPS instant payment provider"),
    EBA_STEP2_PROVIDER("EBA STEP2 payment provider"),
    INTERNAL_PROVIDER("Internal transfer provider"),
    DEFAULT_PROVIDER("Default payment provider");
    
    private final String description;
    
    PaymentProviderType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

#### PaymentStatus

Defines the possible statuses of a payment:

```java
public enum PaymentStatus {
    PENDING("Payment is pending processing"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment has been completed successfully"),
    REJECTED("Payment has been rejected"),
    CANCELLED("Payment has been cancelled"),
    SCHEDULED("Payment is scheduled for future execution"),
    FAILED("Payment processing has failed"),
    RETURNED("Payment has been returned by the receiving bank"),
    RECALLED("Payment has been recalled by the sender"),
    EXPIRED("Payment has expired"),
    UNKNOWN("Payment status is unknown");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

#### PaymentOperationType

Defines the types of payment operations:

```java
public enum PaymentOperationType {
    SIMULATE("Simulate payment"),
    EXECUTE("Execute payment"),
    CANCEL("Cancel payment"),
    SCHEDULE("Schedule payment"),
    STATUS("Check payment status"),
    RECALL("Recall payment"),
    RETURN("Return payment"),
    REFUND("Refund payment");
    
    private final String description;
    
    PaymentOperationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

## Data Model Relationships

The data models in the payment hub form a network of relationships:

### Inheritance Relationships

Inheritance relationships define the specialization hierarchy:

```
BasePaymentRequestDTO
├── SepaPaymentRequestDTO
├── SwiftPaymentRequestDTO
├── AchTransferRequestDTO
├── UkPaymentRequestDTO
└── InternalTransferRequestDTO
```

### Composition Relationships

Composition relationships define the containment of one data model within another:

```
SepaPaymentRequestDTO
├── SepaAccountDTO (debtorAccount)
└── SepaAccountDTO (creditorAccount)

SwiftPaymentRequestDTO
├── PartyInfoDTO (orderingCustomer)
├── FinancialInstitutionDTO (orderingInstitution)
├── PartyInfoDTO (beneficiaryCustomer)
└── FinancialInstitutionDTO (beneficiaryInstitution)

AchTransferRequestDTO
├── PartyInfoDTO (originator)
├── AccountInfoDTO (originatorAccount)
├── PartyInfoDTO (receiver)
└── AccountInfoDTO (receiverAccount)

UkPaymentRequestDTO
├── UkAccountDTO (debtorAccount)
└── UkAccountDTO (creditorAccount)
```

### Association Relationships

Association relationships define the connections between different data models:

```
BasePaymentRequestDTO -- PaymentType
PaymentSimulationResultDTO -- PaymentType
PaymentSimulationResultDTO -- PaymentProviderType
PaymentSimulationResultDTO -- PaymentOperationType
PaymentExecutionResultDTO -- PaymentStatus
```

## Data Validation

The payment hub implements comprehensive data validation using Bean Validation annotations:

### Field-Level Validation

Validation of individual fields:

```java
@NotBlank(message = "Request ID is required")
private String requestId;

@NotNull(message = "Amount is required")
@Positive(message = "Amount must be positive")
private BigDecimal amount;

@Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
private String currency;
```

### Object-Level Validation

Validation of entire objects:

```java
@Valid
@NotNull(message = "Debtor account is required")
private SepaAccountDTO debtorAccount;
```

### Custom Validation

Custom validation logic for complex rules:

```java
@Component
public class SepaPaymentValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return SepaPaymentRequestDTO.class.isAssignableFrom(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        SepaPaymentRequestDTO request = (SepaPaymentRequestDTO) target;
        
        // Validate IBAN
        if (request.getDebtorAccount() != null && request.getDebtorAccount().getIban() != null) {
            if (!IbanValidator.isValid(request.getDebtorAccount().getIban())) {
                errors.rejectValue("debtorAccount.iban", "invalid.iban", "Invalid IBAN");
            }
        }
        
        if (request.getCreditorAccount() != null && request.getCreditorAccount().getIban() != null) {
            if (!IbanValidator.isValid(request.getCreditorAccount().getIban())) {
                errors.rejectValue("creditorAccount.iban", "invalid.iban", "Invalid IBAN");
            }
        }
        
        // Validate BIC if provided
        if (request.getDebtorAccount() != null && request.getDebtorAccount().getBic() != null) {
            if (!BicValidator.isValid(request.getDebtorAccount().getBic())) {
                errors.rejectValue("debtorAccount.bic", "invalid.bic", "Invalid BIC");
            }
        }
        
        if (request.getCreditorAccount() != null && request.getCreditorAccount().getBic() != null) {
            if (!BicValidator.isValid(request.getCreditorAccount().getBic())) {
                errors.rejectValue("creditorAccount.bic", "invalid.bic", "Invalid BIC");
            }
        }
        
        // Validate that debtor and creditor are different
        if (request.getDebtorAccount() != null && request.getCreditorAccount() != null) {
            if (request.getDebtorAccount().getIban() != null && 
                    request.getDebtorAccount().getIban().equals(request.getCreditorAccount().getIban())) {
                errors.reject("same.accounts", "Debtor and creditor accounts cannot be the same");
            }
        }
    }
}
```

## Data Mapping

The payment hub uses object mapping to convert between different data models:

### DTO to Domain Mapping

Mapping from DTOs to domain objects:

```java
@Component
public class PaymentMapper {
    public SepaPayment mapToSepaPayment(SepaPaymentRequestDTO dto) {
        return SepaPayment.builder()
                .requestId(dto.getRequestId())
                .paymentType(dto.getPaymentType())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .debtorAccount(mapToSepaAccount(dto.getDebtorAccount()))
                .creditorAccount(mapToSepaAccount(dto.getCreditorAccount()))
                .remittanceInformation(dto.getRemittanceInformation())
                .endToEndId(dto.getEndToEndId())
                .chargeBearer(dto.getChargeBearer())
                .requestedExecutionDate(dto.getRequestedExecutionDate())
                .build();
    }
    
    private SepaAccount mapToSepaAccount(SepaAccountDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return SepaAccount.builder()
                .iban(dto.getIban())
                .bic(dto.getBic())
                .accountName(dto.getAccountName())
                .build();
    }
    
    // Other mapping methods...
}
```

### Domain to DTO Mapping

Mapping from domain objects to DTOs:

```java
public PaymentExecutionResultDTO mapToExecutionResultDTO(Payment payment, PaymentExecution execution) {
    return PaymentExecutionResultDTO.builder()
            .paymentId(payment.getId())
            .requestId(payment.getRequestId())
            .paymentType(payment.getPaymentType())
            .operationType(PaymentOperationType.EXECUTE)
            .provider(execution.getProvider())
            .timestamp(execution.getTimestamp())
            .success(execution.isSuccess())
            .status(execution.getStatus())
            .rejectReason(execution.getRejectReason())
            .providerReference(execution.getProviderReference())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .fee(execution.getFee())
            .feeType(execution.getFeeType())
            .executionDate(execution.getExecutionDate())
            .expectedSettlementDate(execution.getExpectedSettlementDate())
            .transactionReference(execution.getTransactionReference())
            .clearingSystemReference(execution.getClearingSystemReference())
            .receivedTimestamp(execution.getReceivedTimestamp())
            .requiresAuthorization(execution.isRequiresAuthorization())
            .scaRequired(execution.isScaRequired())
            .scaCompleted(execution.isScaCompleted())
            .scaResult(execution.getScaResult())
            .additionalInfo(execution.getAdditionalInfo())
            .build();
}
```

## Serialization and Deserialization

The payment hub uses JSON for serialization and deserialization of data models:

### JSON Serialization

Serialization of data models to JSON:

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestId",
    "paymentType",
    "amount",
    "currency",
    "debtorAccount",
    "creditorAccount",
    "remittanceInformation",
    "endToEndId",
    "chargeBearer",
    "requestedExecutionDate",
    "sca"
})
public class SepaPaymentRequestDTO extends BasePaymentRequestDTO {
    // Fields and methods...
}
```

### JSON Deserialization

Deserialization of JSON to data models:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasePaymentRequestDTO {
    // Fields and methods...
}
```

### Custom Serialization

Custom serialization for complex types:

```java
@JsonSerialize(using = MoneySerializer.class)
@JsonDeserialize(using = MoneyDeserializer.class)
private Money amount;

public static class MoneySerializer extends JsonSerializer<Money> {
    @Override
    public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("amount", value.getAmount());
        gen.writeStringField("currency", value.getCurrency().getCurrencyCode());
        gen.writeEndObject();
    }
}

public static class MoneyDeserializer extends JsonDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        BigDecimal amount = new BigDecimal(node.get("amount").asText());
        String currency = node.get("currency").asText();
        return Money.of(amount, currency);
    }
}
```

## Best Practices

### Data Model Design

When designing data models:

1. **Use inheritance** to share common attributes
2. **Use composition** for complex objects
3. **Apply validation annotations** at the field level
4. **Document all fields** with clear descriptions
5. **Use appropriate data types** for each field
6. **Follow naming conventions** consistently
7. **Keep models focused** on a single responsibility
8. **Consider serialization** requirements
9. **Design for extensibility** to accommodate future changes
10. **Minimize duplication** across models

### Data Validation

When implementing data validation:

1. **Validate at multiple levels** (field, object, business logic)
2. **Provide clear error messages** for validation failures
3. **Use custom validators** for complex rules
4. **Consider cross-field validation** for related fields
5. **Validate early** to fail fast
6. **Handle validation errors** consistently
7. **Log validation failures** for troubleshooting
8. **Test validation rules** thoroughly
9. **Document validation requirements** for developers
10. **Consider internationalization** of error messages

### Data Mapping

When implementing data mapping:

1. **Use a consistent mapping approach** across the system
2. **Consider using a mapping library** for complex mappings
3. **Test mappings** with various input scenarios
4. **Handle null values** appropriately
5. **Document mapping rules** for complex transformations
6. **Consider performance** for large-scale mappings
7. **Validate mapped objects** after mapping
8. **Use mapping profiles** for different contexts
9. **Keep mapping logic separate** from business logic
10. **Consider bidirectional mapping** needs

## Conclusion

The data models in the Firefly Core Banking Payment Hub provide a comprehensive and flexible foundation for representing payment-related information. By following a consistent design approach with inheritance, composition, and proper validation, the data models support the diverse requirements of different payment types while maintaining consistency and extensibility.

The careful design of these data models ensures that the payment hub can handle the complex data requirements of modern payment systems while providing a clean and intuitive API for developers.

## References

1. Fowler, M. - [Patterns of Enterprise Application Architecture](https://martinfowler.com/books/eaa.html)
2. Evans, E. - [Domain-Driven Design](https://domainlanguage.com/ddd/)
3. Bean Validation - [Jakarta Bean Validation Specification](https://beanvalidation.org/2.0/spec/)
4. Jackson - [Jackson Documentation](https://github.com/FasterXML/jackson-docs)
5. Lombok - [Lombok Documentation](https://projectlombok.org/features/)
