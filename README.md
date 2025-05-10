# Firefly Core Banking Payment Hub

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
  - [Hexagonal Architecture](#hexagonal-architecture)
  - [Module Structure](#module-structure)
  - [Key Components](#key-components)
  - [Provider Registry](#provider-registry)
- [Supported Payment Types](#supported-payment-types)
  - [SEPA Payments](#sepa-payments)
  - [SWIFT Payments](#swift-payments)
  - [ACH Payments](#ach-payments)
  - [UK Payments](#uk-payments)
  - [European Payments](#european-payments)
  - [Internal Transfers](#internal-transfers)
- [Strong Customer Authentication](#strong-customer-authentication)
  - [SCA Implementation](#sca-implementation)
  - [SCA Flow](#sca-flow)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
- [Usage](#usage)
  - [API Documentation](#api-documentation)
  - [Making Payments](#making-payments)
  - [Simulating Payments](#simulating-payments)
  - [Scheduling Payments](#scheduling-payments)
  - [Cancelling Payments](#cancelling-payments)
- [Extending the Payment Hub](#extending-the-payment-hub)
  - [Adding a New Payment Provider](#adding-a-new-payment-provider)
  - [Adding a New Payment Type](#adding-a-new-payment-type)
- [Development](#development)
  - [Building the Project](#building-the-project)
  - [Running Tests](#running-tests)
  - [Code Style and Guidelines](#code-style-and-guidelines)
- [Deployment](#deployment)
  - [Environment Setup](#environment-setup)
  - [Deployment Process](#deployment-process)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

The Firefly Core Banking Payment Hub is a comprehensive payment processing system designed as a central component of the Firefly Core Banking Platform. It provides a unified interface for handling various types of payment operations, including SEPA, SWIFT, ACH, and internal transfers.

The Payment Hub is built on modern, reactive architecture principles, leveraging Spring WebFlux for non-blocking I/O operations, which ensures high throughput and scalability. It follows a hexagonal architecture pattern, allowing for flexible integration with various payment providers while maintaining a clean separation of concerns.

Key features include:

- Support for multiple payment types (SEPA, SWIFT, ACH, UK payments, European payments, internal transfers)
- Dynamic provider discovery and registration
- Payment simulation, execution, cancellation, and scheduling
- Strong Customer Authentication (SCA) for secure payment operations
- Standardized payment provider implementation with common base functionality
- Comprehensive metrics collection using Micrometer
- Advanced biometric authentication support (fingerprint, face, voice, iris, etc.)
- Reactive programming model with non-blocking I/O
- Extensible architecture for adding new payment types and providers
- Comprehensive API documentation with OpenAPI/Swagger
- ABA routing support for USA transfers

## Architecture

### Hexagonal Architecture

The Payment Hub implements the hexagonal architecture (also known as ports and adapters) pattern, which provides a clear separation between:

1. **Core Domain Logic** - The business rules and use cases of the payment system
2. **Ports** - Interfaces that define how the core domain interacts with the outside world
3. **Adapters** - Implementations of the ports that connect to external systems

This architecture allows the core business logic to remain isolated from external concerns, making the system more maintainable, testable, and adaptable to changing requirements.

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         HEXAGONAL ARCHITECTURE                             │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                              Core Domain                             │  │
│  │                                                                      │  │
│  │  ┌────────────────────┐ ┌────────────────────┐ ┌──────────────────┐  │  │
│  │  │   Service Layer    │ │   Domain Models    │ │       DTOs       │  │  │
│  │  └────────────────────┘ └────────────────────┘ └──────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                              │                                             │
│                              ▼                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                                Ports                                 │  │
│  │                                                                      │  │
│  │  ┌────────────────────────────────────────────────────────────────┐  │  │
│  │  │                     Provider Interfaces                        │  │  │
│  │  │                                                                │  │  │
│  │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐               │  │  │
│  │  │  │  SEPA   │ │  SWIFT  │ │   ACH   │ │   UK    │               │  │  │
│  │  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘               │  │  │
│  │  │  ┌─────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐             │  │  │
│  │  │  │TARGET2  │ │  TIPS   │ │EBA STEP2 │ │ Internal │             │  │  │
│  │  │  └─────────┘ └─────────┘ └──────────┘ └──────────┘             │  │  │
│  │  └────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                              │                                             │
│                              ▼                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                             Adapters                                 │  │
│  │                                                                      │  │
│  │  ┌──────────────────────────────┐ ┌──────────────────────────────┐   │  │
│  │  │ Provider Implementations     │ │       REST Controllers       │   │  │
│  │  │                              │ │                              │   │  │
│  │  │  ┌─────────┐ ┌─────────┐     │ │  ┌────────────┐ ┌──────────┐ │   │  │
│  │  │  │ Default │ │ Default │     │ │  │   SEPA     │ │  SWIFT   │ │   │  │
│  │  │  │  SEPA   │ │  SWIFT  │     │ │  │ Controller │ │Controller│ │   │  │
│  │  │  └─────────┘ └─────────┘     │ │  └────────────┘ └──────────┘ │   │  │
│  │  │  ┌─────────┐ ┌─────────┐     │ │  ┌────────────┐ ┌──────────┐ │   │  │
│  │  │  │ Default │ │ Default │     │ │  │   ACH      │ │ European │ │   │  │
│  │  │  │   ACH   │ │   UK    │     │ │  │ Controller │ │Controller│ │   │  │
│  │  │  └─────────┘ └─────────┘     │ │  └────────────┘ └──────────┘ │   │  │
│  │  │  ┌─────────┐ ┌─────────┐     │ │  ┌────────────┐ ┌──────────┐ │   │  │
│  │  │  │ Default │ │ Default │     │ │  │    UK      │ │ Internal │ │   │  │
│  │  │  │ TARGET2 │ │  TIPS   │     │ │  │ Controller │ │Controller│ │   │  │
│  │  │  └─────────┘ └─────────┘     │ │  └────────────┘ └──────────┘ │   │  │
│  │  └──────────────────────────────┘ └──────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘


```

### Module Structure

The Payment Hub is organized into three main modules:

1. **core-banking-payment-hub-interfaces**
   - Contains the domain models, DTOs, and provider interfaces
   - Defines the contracts between the core domain and external systems
   - Includes enums for payment types and provider types

2. **core-banking-payment-hub-core**
   - Contains the core business logic and service implementations
   - Implements the provider registry for dynamic provider discovery
   - Handles payment routing and processing logic

3. **core-banking-payment-hub-web**
   - Contains the REST API controllers and web-related configurations
   - Exposes the payment operations as HTTP endpoints
   - Includes OpenAPI/Swagger documentation

This modular structure ensures a clean separation of concerns and allows for independent evolution of each component.

### Key Components

#### Abstract Base Provider

The Payment Hub implements a standardized approach to payment providers through an abstract base implementation:

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

This abstract base class provides:
- Standardized SCA handling across all providers
- Consistent error handling and response formatting
- Comprehensive metrics collection
- Common health check implementation

#### Service Layer

The service layer defines the core business operations for each payment type. Each service follows a consistent pattern with methods for:

- **simulate** - Validates payment data and triggers SCA if needed
- **executePayment** - Processes the actual payment with SCA verification
- **simulateCancellation** - Validates cancellation feasibility and triggers SCA if needed
- **cancelPayment** - Processes the actual cancellation with SCA verification
- **schedulePayment** - Schedules a payment for future execution

The Payment Hub includes the following service interfaces:

- `PaymentService<T, C, S>` - Generic interface for common payment operations
- `SepaPaymentService` - SEPA-specific payment operations
- `SwiftPaymentService` - SWIFT-specific payment operations
- `AchPaymentService` - ACH-specific payment operations with ABA routing support
- `UkPaymentService` - UK-specific payment operations
- `Target2PaymentService` - TARGET2-specific payment operations
- `TipsPaymentService` - TIPS-specific payment operations
- `EbaStep2PaymentService` - EBA STEP2-specific payment operations
- `InternalTransferService` - Internal transfer operations

Each service implementation handles the business logic for its specific payment type, including validation, routing, fee calculation, and integration with external payment networks.

#### Provider Interfaces

Provider interfaces define the contracts that payment providers must implement:

- `BasePaymentProvider` - Common interface for all payment providers
- `SepaPaymentProvider` - Interface for SEPA payment providers
- `SwiftPaymentProvider` - Interface for SWIFT payment providers
- `AchPaymentProvider` - Interface for ACH payment providers
- `UkPaymentProvider` - Interface for UK payment providers
- `Target2PaymentProvider` - Interface for TARGET2 payment providers
- `TipsPaymentProvider` - Interface for TIPS payment providers
- `EbaStep2PaymentProvider` - Interface for EBA STEP2 payment providers
- `InternalTransferProvider` - Interface for internal transfer providers

All provider implementations extend the `AbstractBasePaymentProvider` class, which provides standardized functionality for common operations. This approach ensures consistent behavior across all payment providers and reduces code duplication.

#### DTOs (Data Transfer Objects)

DTOs are used to transfer data between the different layers of the application. The Payment Hub includes several categories of DTOs:

- **Request DTOs**:
  - Payment Request DTOs (e.g., `SepaPaymentRequestDTO`, `SwiftPaymentRequestDTO`)
  - Cancellation Request DTOs (e.g., `SepaCancellationRequestDTO`, `SwiftCancellationRequestDTO`)
  - Schedule Request DTOs (e.g., `PaymentScheduleRequestDTO`)

- **Result DTOs**:
  - `PaymentSimulationResultDTO` - Results of payment or cancellation simulation
  - `PaymentExecutionResultDTO` - Results of payment execution
  - `PaymentCancellationResultDTO` - Results of payment cancellation
  - `PaymentScheduleResultDTO` - Results of payment scheduling

- **Authentication DTOs**:
  - `ScaDTO` - Strong Customer Authentication information
  - `ScaResultDTO` - Results of SCA verification

- **Common DTOs**:
  - Account DTOs (e.g., `IbanAccountDTO`, `UkAccountDTO`, `AbaAccountDTO`)
  - Party DTOs (e.g., `PartyDTO`, `FinancialInstitutionDTO`)
  - Reference DTOs (e.g., `PaymentReferenceDTO`)

All DTOs use Lombok annotations (`@Data`, `@Builder`, etc.) for clean, concise code and include comprehensive validation annotations and Swagger documentation.

#### Controllers

REST controllers expose the payment operations as HTTP endpoints. Each controller provides a comprehensive set of operations:

- **Simulation** (`/simulate`) - Validates the payment without execution and triggers SCA if needed
- **Execution** (`/execute`) - Processes the actual payment with SCA verification
- **Cancellation Simulation** (`/cancel/simulate`) - Validates cancellation feasibility and triggers SCA if needed
- **Cancellation** (`/cancel`) - Processes the actual cancellation with SCA verification
- **Scheduling** (`/schedule`) - Schedules a payment for future execution

The Payment Hub includes the following controllers:

- `SepaPaymentController` - Endpoints for SEPA payments (SCT, SCT Inst, SDD)
- `SwiftPaymentController` - Endpoints for SWIFT payments (MT103, MT202, PACS.008)
- `AchPaymentController` - Endpoints for ACH payments with ABA routing support
- `UkPaymentController` - Endpoints for UK payments (FPS, BACS, CHAPS)
- `EuropeanPaymentController` - Endpoints for European payments (TARGET2, TIPS, EBA STEP2)
- `InternalTransferController` - Endpoints for internal transfers

Each controller is annotated with comprehensive OpenAPI/Swagger annotations that provide detailed information about:
- The payment type and its characteristics
- The specific process steps for each operation
- Required and optional fields
- Expected responses and error conditions
- SCA requirements and handling

### Provider Registry

The Provider Registry is a key component that implements the hexagonal architecture pattern by dynamically discovering and registering available payment providers at runtime.

```java
@Component
public class PaymentProviderRegistry {
    // ...

    @PostConstruct
    public void initialize() {
        discoverProviders();
        mapPaymentTypesToProviders();
        logAvailableProviders();
    }

    private void discoverProviders() {
        // Discover SEPA providers
        Map<String, SepaPaymentProvider> sepaProviders =
            applicationContext.getBeansOfType(SepaPaymentProvider.class);
        if (!sepaProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.SEPA_PROVIDER,
                sepaProviders.values().iterator().next());
        }

        // Similar code for other provider types...
    }

    // ...
}
```

This approach allows the system to:

1. Automatically discover provider implementations at runtime
2. Map payment types to appropriate providers
3. Select the right provider for each payment operation
4. Easily add new providers without changing the core logic

## Supported Payment Types

### SEPA Payments

The Payment Hub supports the following SEPA (Single Euro Payments Area) payment types:

- **SEPA Credit Transfer (SCT)** - Standard credit transfers within the SEPA zone
- **SEPA Instant Credit Transfer (ICT)** - Real-time credit transfers within the SEPA zone
- **SEPA Direct Debit (SDD)** - Direct debit payments within the SEPA zone
- **SEPA Direct Debit Core (SDD Core)** - Core direct debit scheme for consumers
- **SEPA Direct Debit B2B (SDD B2B)** - Business-to-business direct debit scheme
- **SEPA SCT Future Dated** - Credit transfers scheduled for future execution
- **SEPA SCT Standing Order** - Recurring credit transfers

SEPA payments use IBAN (International Bank Account Number) and BIC (Bank Identifier Code) for account identification.

### SWIFT Payments

The Payment Hub supports the following SWIFT payment types:

- **MT103** - Customer Credit Transfer
- **MT202** - Financial Institution Transfer
- **PACS.008** - Customer Credit Transfer (ISO 20022 format)

SWIFT payments are used for international transfers outside the SEPA zone.

### ACH Payments

The Payment Hub supports the following ACH (Automated Clearing House) payment types for US bank transfers:

- **ACH Credit** - Push payments from the originator to the receiver
- **ACH Debit** - Pull payments initiated by the receiver
- **Wire Transfer** - Real-time, irrevocable transfers between US banks

ACH payments use ABA routing numbers (9-digit codes) and account numbers for account identification, ensuring compatibility with the US banking system.

### UK Payments

The Payment Hub supports the following UK payment types:

- **Faster Payments Service (FPS)** - Real-time payments within the UK
- **BACS Direct Credit** - Standard UK credit transfers (3-day settlement)
- **BACS Direct Debit** - UK direct debit payments
- **CHAPS** - Same-day high-value payments within the UK
- **UK Standing Order** - Recurring UK payments

UK payments use sort codes and account numbers for account identification.

### European Payments

The Payment Hub supports the following European payment types:

- **TARGET2** - Real-time gross settlement system for euro payments
- **TIPS** - TARGET Instant Payment Settlement for instant euro payments
- **EBA STEP2** - Pan-European Automated Clearing House for euro payments

European payments typically use IBAN and BIC for account identification.

### Internal Transfers

The Payment Hub supports internal transfers between accounts within the core banking system:

- **Internal Transfer** - Transfer between two accounts
- **Internal Bulk Transfer** - Multiple transfers processed as a batch
- **Internal Standing Order** - Recurring internal transfers
- **Internal Future Dated Transfer** - Internal transfers scheduled for future execution

Internal transfers are processed immediately within the core banking system without using external payment networks.

## Strong Customer Authentication

The Payment Hub implements Strong Customer Authentication (SCA) to comply with regulatory requirements and enhance payment security. SCA is a requirement for electronic payments where the user must provide at least two of the following authentication factors:

1. **Knowledge** - Something only the user knows (e.g., password, PIN)
2. **Possession** - Something only the user possesses (e.g., mobile phone, hardware token)
3. **Inherence** - Something the user is (e.g., fingerprint, facial recognition)

### SCA Implementation

The Payment Hub's SCA implementation includes:

- **Dedicated SCA Provider** - SCA is implemented as a dedicated provider interface (`ScaProvider`) with methods for triggering and validating SCA
- **Provider Delegation** - Payment providers delegate SCA operations to the SCA provider through the `AbstractBasePaymentProvider` class
- **Dynamic SCA Requirements** - SCA is required based on payment amount, destination country, and other risk factors
- **Multiple Authentication Methods** - Support for SMS, email, mobile app, and various biometric authentication methods
- **Advanced Biometric Authentication** - Support for fingerprint, facial recognition, voice recognition, iris scan, retina scan, palm print, vein pattern, and behavioral biometrics
- **Challenge-Response Flow** - Secure challenge-response mechanism for authentication
- **SCA for Critical Operations** - Authentication for payment execution, scheduling, and cancellation
- **Comprehensive Coverage** - SCA is implemented for all payment types (SEPA, SWIFT, ACH, UK, European, Internal)
- **Regulatory Compliance** - Complies with PSD2 requirements for European payments and similar regulations for other regions
- **Health Monitoring** - SCA provider health is monitored through dedicated health indicators
- **Metrics Collection** - Comprehensive metrics for SCA operations using Micrometer

### SCA Flow

1. **Simulation Phase**:
   - The system determines if SCA is required for the payment
   - If required, the simulation triggers SCA delivery (SMS, email, etc.)
   - The simulation result includes:
     - `scaRequired` flag
     - `scaDeliveryTriggered` flag
     - `scaDeliveryMethod` (SMS, EMAIL, APP, etc.)
     - `scaDeliveryRecipient` (masked for privacy)
     - `simulationReference` (to link subsequent operations)

2. **Authentication Phase**:
   - The user receives the authentication code via the delivery method
   - The client includes the SCA information and simulation reference in the subsequent request
   - The system validates the authentication code against the previously delivered code

3. **Execution Phase**:
   - If SCA validation is successful, the payment is processed
   - If SCA validation fails, the payment is rejected with an appropriate error message

Example SCA information in a payment request with traditional authentication:

```json
{
  "sca": {
    "method": "SMS",
    "recipient": "+34600000000",
    "authenticationCode": "123456"
  }
}
```

Example SCA information with biometric authentication:

```json
{
  "sca": {
    "method": "BIOMETRIC_FINGERPRINT",
    "deviceId": "DEVICE-123456",
    "biometricData": "VERIFICATION-TOKEN-123456"
  }
}
```

## Getting Started

### Prerequisites

To build and run the Firefly Core Banking Payment Hub, you need:

- Java 17 or later
- Maven 3.6 or later
- Git
- Docker (optional, for containerized deployment)

### Installation

1. Clone the repository:

```bash
git clone https://github.com/firefly-oss/core-banking-payment-hub.git
cd core-banking-payment-hub
```

2. Build the project:

```bash
mvn clean install
```

3. Run the application:

```bash
cd core-banking-payment-hub-web
mvn spring-boot:run
```

### Configuration

The Payment Hub can be configured using standard Spring Boot configuration mechanisms:

1. **application.properties/application.yml** - Main configuration file
2. **Environment variables** - For environment-specific configuration
3. **System properties** - For runtime configuration

Key configuration properties include:

```yaml
spring:
  application:
    name: Firefly Core Banking Payment Hub
    version: 1.0.0
    description: Payment processing system for the Firefly Core Banking Platform
    team:
      name: Firefly Core Banking Team
      email: core-banking@firefly.com

# Payment provider configuration
payment:
  providers:
    sepa:
      enabled: true
      timeout: 30000
    swift:
      enabled: true
      timeout: 60000
    ach:
      enabled: true
      timeout: 30000
    uk:
      enabled: true
      timeout: 20000
    target2:
      enabled: true
      timeout: 30000
    tips:
      enabled: true
      timeout: 15000
    eba_step2:
      enabled: true
      timeout: 30000
    internal:
      enabled: true
      timeout: 10000
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
        - BIOMETRIC_IRIS
        - BIOMETRIC_RETINA
        - BIOMETRIC_PALM
        - BIOMETRIC_VEIN
        - BIOMETRIC_BEHAVIORAL
      expiry-minutes: 5  # Shorter expiry for biometric authentication
  metrics:
    enabled: true
    export-to-prometheus: true
```

## Usage

### API Documentation

The Payment Hub provides comprehensive API documentation using OpenAPI/Swagger. Once the application is running, you can access the API documentation at:

```
http://localhost:8080/swagger-ui.html
```

The documentation includes:

- Detailed descriptions of all endpoints
- Request and response schemas
- Example requests and responses
- Authentication requirements

### Making Payments

To execute a payment, send a POST request to the appropriate endpoint with the payment details. For high-value payments, Strong Customer Authentication (SCA) may be required:

```bash
# 1. First, simulate the SEPA payment to trigger SCA delivery
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

# 2. Then execute the payment with the SCA code and simulation reference
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
    "simulationReference": "SIM-12345678",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

### Simulating Payments

The simulate operation serves three key purposes:
1. It provides information like fees, estimated execution dates, etc.
2. It triggers SCA (Strong Customer Authentication) delivery when required
3. It returns a simulation reference that should be used in subsequent execute, schedule, or cancel operations

Similarly, the cancellation simulation operation:
1. Validates if the payment can be cancelled
2. Provides information about cancellation fees and implications
3. Triggers SCA delivery when required for high-value payment cancellations
4. Returns a simulation reference that should be used in the actual cancellation request

```bash
# SWIFT payment simulation example
curl -X POST http://localhost:8080/api/v1/payments/swift/simulate \
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
    "paymentDetails": "International payment"
  }'
```

### Scheduling Payments

To schedule a payment for future execution, first simulate the payment to trigger SCA if required, then use the `/schedule` endpoint with the SCA code and simulation reference:

```bash
# 1. First, simulate the SWIFT payment to trigger SCA delivery
curl -X POST http://localhost:8080/api/v1/payments/swift/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "REQ-12347",
    "paymentType": "SWIFT_MT103",
    "amount": 5000.00,
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
    "paymentDetails": "International payment"
  }'

# 2. Then schedule the payment with the SCA code and simulation reference
curl -X POST http://localhost:8080/api/v1/payments/swift/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "paymentRequest": {
      "requestId": "REQ-12347",
      "paymentType": "SWIFT_MT103",
      "amount": 5000.00,
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
      "simulationReference": "SIM-12345678",
      "beneficiaryInstitution": {
        "bic": "BARCGB22",
        "bankName": "Barclays Bank",
        "bankCountryCode": "GB"
      },
      "paymentDetails": "International payment",
      "sca": {
        "method": "SMS",
        "recipient": "+34600000000",
        "authenticationCode": "123456"
      }
    },
    "executionDate": "2023-12-31",
    "executionTime": "14:30:00",
    "recurrencePattern": "0 0 1 1 * ?",
    "recurrenceEndDate": "2024-12-31",
    "description": "Monthly international payment"
  }'
```

### Cancelling Payments

The Payment Hub implements a two-step process for cancelling payments:

1. **Simulation Step**: First, simulate the cancellation to check if it's possible and to trigger SCA if required
2. **Cancellation Step**: Then, perform the actual cancellation with the SCA code and simulation reference

This approach ensures that users are informed about the feasibility of cancellation before proceeding, and provides proper authentication for sensitive operations.

#### Cancellation Simulation

The cancellation simulation process:
- Validates the cancellation request
- Checks if the payment exists and is in a cancellable state
- Determines if Strong Customer Authentication (SCA) is required
- Delivers SCA code if required (via SMS or other configured channel)
- Returns information about cancellation feasibility, fees, and SCA requirements

```bash
# SEPA payment cancellation simulation example
curl -X POST http://localhost:8080/api/v1/payments/sepa/cancel/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678",
    "paymentType": "SEPA_SCT",
    "cancellationReason": "Customer request",
    "additionalInformation": "Payment no longer needed"
  }'
```

```bash
# SWIFT payment cancellation simulation example
curl -X POST http://localhost:8080/api/v1/payments/swift/cancel/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678",
    "paymentType": "SWIFT_MT103",
    "cancellationReason": "Customer request",
    "additionalInformation": "Payment no longer needed"
  }'
```

```bash
# Internal transfer cancellation simulation example
curl -X POST http://localhost:8080/api/v1/payments/internal/cancel/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "INT-12345678",
    "paymentType": "INTERNAL_TRANSFER",
    "sourceAccountId": "ACC-12345678",
    "destinationAccountId": "ACC-87654321",
    "cancellationReason": "Customer request",
    "additionalInformation": "Transfer no longer needed"
  }'
```

#### Actual Cancellation

After simulation, proceed with the actual cancellation using the simulation reference and SCA information:

```bash
# SEPA payment cancellation example
curl -X POST http://localhost:8080/api/v1/payments/sepa/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678",
    "paymentType": "SEPA_SCT",
    "cancellationReason": "Customer request",
    "additionalInformation": "Payment no longer needed",
    "simulationReference": "SIM-12345678",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

```bash
# SWIFT payment cancellation example
curl -X POST http://localhost:8080/api/v1/payments/swift/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678",
    "paymentType": "SWIFT_MT103",
    "cancellationReason": "Customer request",
    "additionalInformation": "Payment no longer needed",
    "simulationReference": "SIM-12345678",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

```bash
# Internal transfer cancellation example
curl -X POST http://localhost:8080/api/v1/payments/internal/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "INT-12345678",
    "paymentType": "INTERNAL_TRANSFER",
    "sourceAccountId": "ACC-12345678",
    "destinationAccountId": "ACC-87654321",
    "cancellationReason": "Customer request",
    "additionalInformation": "Transfer no longer needed",
    "simulationReference": "SIM-12345678",
    "sca": {
      "method": "SMS",
      "recipient": "+34600000000",
      "authenticationCode": "123456"
    }
  }'
```

Note that cancellation capabilities vary by payment type:
- **SEPA Instant Credit Transfers**: Very short cancellation window due to real-time settlement
- **SWIFT payments**: Difficult to cancel once they've entered the correspondent banking network
- **ACH payments**: Can typically be cancelled before the settlement date
- **UK Faster Payments**: Very short cancellation window due to near real-time settlement
- **Internal transfers**: Usually cancellable if not yet fully processed

## Extending the Payment Hub

### Adding a New Payment Provider

To add a new payment provider, follow these steps:

1. Implement the appropriate provider interface by extending the abstract base class:

```java
@Component
public class NewSepaPaymentProvider extends AbstractBasePaymentProvider implements SepaPaymentProvider {

    @Autowired
    public NewSepaPaymentProvider(ScaProvider scaProvider) {
        super(scaProvider);
    }

    @Override
    public Mono<PaymentSimulationResultDTO> simulate(SepaPaymentRequestDTO request) {
        // Implementation
    }

    @Override
    public Mono<PaymentExecutionResultDTO> execute(SepaPaymentRequestDTO request) {
        // Implementation
    }

    @Override
    protected Mono<Boolean> checkProviderHealth() {
        // Provider-specific health check implementation
        return Mono.just(true);
    }

    @Override
    protected String getProviderName() {
        return "new-sepa";
    }

    // Implement other methods...
}
```

2. Register the provider bean in your Spring configuration:

```java
@Configuration
public class NewProviderConfig {

    @Bean
    public SepaPaymentProvider newSepaPaymentProvider() {
        return new NewSepaPaymentProvider();
    }
}
```

The Provider Registry will automatically discover and register your new provider at runtime.

### Adding a New Payment Type

To add a new payment type, follow these steps:

1. Add the new payment type to the `PaymentType` enum:

```java
public enum PaymentType {
    // Existing payment types...

    // New payment type
    NEW_PAYMENT_TYPE("New Payment Type");

    // Rest of the enum...
}
```

2. Create DTOs for the new payment type:

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewPaymentRequestDTO extends BasePaymentRequestDTO {
    // New payment type specific fields
}
```

3. Create a provider interface for the new payment type:

```java
public interface NewPaymentProvider {
    Mono<PaymentSimulationResultDTO> simulate(NewPaymentRequestDTO request);
    Mono<PaymentExecutionResultDTO> execute(NewPaymentRequestDTO request);
    // Other methods...
}
```

4. Update the Provider Registry to handle the new payment type:

```java
private void mapPaymentTypesToProviders() {
    // Existing mappings...

    // Map new payment type to provider
    paymentTypeToProviderMap.put(PaymentType.NEW_PAYMENT_TYPE,
        PaymentProviderType.NEW_PROVIDER);
}
```

5. Create a service interface and implementation for the new payment type.

6. Create a controller for the new payment type.

## Development

### Building the Project

To build the entire project:

```bash
mvn clean install
```

To build a specific module:

```bash
cd core-banking-payment-hub-core
mvn clean install
```

### Running Tests

To run all tests:

```bash
mvn test
```

To run tests for a specific module:

```bash
cd core-banking-payment-hub-interfaces
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=PaymentProviderRegistryTest
```

### Code Style and Guidelines

The project follows these coding guidelines:

1. Use Lombok annotations to reduce boilerplate code
2. Follow the hexagonal architecture pattern
3. Use reactive programming with Reactor
4. Document all public APIs with Javadoc and OpenAPI annotations
5. Write unit tests for all business logic
6. Use meaningful names for classes, methods, and variables

## Deployment

### Environment Setup

The Payment Hub can be deployed in various environments:

1. **Development** - Local development environment
2. **Testing** - Automated testing environment
3. **Staging** - Pre-production environment
4. **Production** - Live environment

Each environment should have its own configuration profile.

### Deployment Process

1. Build the application:

```bash
mvn clean package
```

2. Create a Docker image:

```bash
docker build -t firefly/payment-hub:latest .
```

3. Deploy the Docker image to your environment:

```bash
docker run -p 8080:8080 firefly/payment-hub:latest
```

4. Configure environment-specific properties using environment variables:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e PAYMENT_PROVIDERS_SEPA_ENABLED=true \
  firefly/payment-hub:latest
```

## Troubleshooting

### Common Issues

1. **Provider not found**
   - Check that the provider implementation is correctly annotated with `@Component`
   - Verify that the provider is in the component scan path

2. **Payment validation errors**
   - Check the request payload against the API documentation
   - Ensure all required fields are provided

3. **Connection issues with external providers**
   - Check network connectivity
   - Verify provider credentials and configuration

4. **SCA validation failures**
   - Ensure the SCA information is correctly provided
   - Check that the authentication code is valid
   - Verify that the SCA method is supported

### Logging

The Payment Hub uses SLF4J for logging. You can configure the log level in your `application.properties`:

```properties
logging.level.com.catalis.core.banking.payments=DEBUG
```

For production environments, set the log level to INFO or WARN.

## Contributing

We welcome contributions to the Firefly Core Banking Payment Hub! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows our coding guidelines and includes appropriate tests.

## License

This project is under the Apache 2.0—LICENSE
