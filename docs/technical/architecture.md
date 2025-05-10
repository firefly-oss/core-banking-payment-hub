# Architecture Overview

## Introduction

The Firefly Core Banking Payment Hub is designed with a modern, scalable, and flexible architecture that follows industry best practices. This document provides a detailed overview of the architectural principles, patterns, and components that form the foundation of the payment hub.

## Architectural Principles

The architecture of the Firefly Core Banking Payment Hub is guided by the following principles:

### Separation of Concerns

Each component has a well-defined responsibility and scope, minimizing dependencies and enabling independent evolution.

### Modularity

The system is divided into cohesive modules that can be developed, tested, and deployed independently.

### Extensibility

The architecture allows for easy addition of new payment types, providers, and features without modifying existing code.

### Scalability

The system can handle increasing loads by scaling horizontally or vertically as needed.

### Resilience

The architecture includes mechanisms for fault tolerance, error handling, and recovery to ensure high availability.

### Security by Design

Security considerations are integrated into the architecture from the ground up, not added as an afterthought.

### Observability

The system provides comprehensive logging, monitoring, and tracing capabilities for operational visibility.

## Hexagonal Architecture

The Firefly Core Banking Payment Hub implements the hexagonal architecture pattern (also known as ports and adapters), which provides a clear separation between:

1. **Core Domain Logic** - The business rules and use cases of the payment system
2. **Ports** - Interfaces that define how the core domain interacts with the outside world
3. **Adapters** - Implementations of the ports that connect to external systems

This architecture allows the core business logic to remain isolated from external concerns, making the system more maintainable, testable, and adaptable to changing requirements.

### Core Domain

The core domain contains the essential business logic of the payment hub:

- **Domain Models** - Represent the key business entities and their relationships
- **Services** - Implement the business rules and use cases
- **Repositories** - Define interfaces for data access
- **Events** - Represent significant occurrences in the domain

The core domain is independent of any external frameworks, databases, or UI technologies, focusing solely on the business problem.

### Ports

Ports define the interfaces through which the core domain interacts with the outside world:

- **Primary Ports (Inbound)** - Interfaces that allow external systems to use the core domain
  - Service interfaces
  - API contracts
  - Event listeners

- **Secondary Ports (Outbound)** - Interfaces that the core domain uses to interact with external systems
  - Repository interfaces
  - Provider interfaces
  - External service interfaces
  - Event publishers

### Adapters

Adapters implement the ports to connect the core domain with external systems:

- **Primary Adapters (Inbound)** - Implement primary ports to allow external systems to use the core domain
  - REST controllers
  - GraphQL resolvers
  - Message consumers

- **Secondary Adapters (Outbound)** - Implement secondary ports to connect the core domain with external systems
  - Database repositories
  - Payment provider implementations
  - External service clients
  - Message publishers

## Module Structure

The Firefly Core Banking Payment Hub is organized into three main modules:

### core-banking-payment-hub-interfaces

This module contains the domain models, DTOs, and provider interfaces:

- **Domain Models** - Core business entities
- **DTOs** - Data Transfer Objects for API communication
- **Provider Interfaces** - Contracts for payment providers
- **Enums** - Enumeration types for payment types, statuses, etc.
- **Exceptions** - Custom exception types

This module has minimal dependencies and defines the contracts between the core domain and external systems.

### core-banking-payment-hub-core

This module contains the core business logic and service implementations:

- **Services** - Business logic implementation
- **Provider Registry** - Dynamic provider discovery and management
- **Validators** - Input validation logic
- **Mappers** - Object mapping between domain models and DTOs
- **Configuration** - Core configuration classes

This module depends on the interfaces module but is independent of any specific web or persistence technology.

### core-banking-payment-hub-web

This module contains the REST API controllers and web-related configurations:

- **Controllers** - REST API endpoints
- **Advice** - Global exception handling
- **Security** - Authentication and authorization
- **Documentation** - API documentation (Swagger/OpenAPI)
- **Web Configuration** - Web-specific configuration

This module depends on both the interfaces and core modules and provides the web interface for the payment hub.

## Key Components

### Service Layer

The service layer defines the core business operations for each payment type:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                             Service Layer                                      │
├───────────────────────────────────────────────────────────────────────────────┘
│
▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                         PaymentService<T,C,S> (Generic)                       │
│                                                                               │
│   - simulatePayment(T request): Mono<PaymentSimulationResultDTO>              │
│   - executePayment(T request): Mono<PaymentExecutionResultDTO>                │
│   - cancelPayment(C request): Mono<PaymentCancellationResultDTO>              │
│   - schedulePayment(S request): Mono<PaymentScheduleResultDTO>                │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ├─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┐
       │                 │                 │                 │                 │                 │
       ▼                 ▼                 ▼                 ▼                 ▼                 ▼
┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│    SEPA     │   │    SWIFT    │   │     ACH     │   │     UK      │   │  European   │   │  Internal   │
│   Payment   │   │   Payment   │   │   Payment   │   │   Payment   │   │   Payment   │   │  Transfer   │
│   Service   │   │   Service   │   │   Service   │   │   Service   │   │   Service   │   │   Service   │
└─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘
                                                             │                 │
                                                             │                 │
                                                             ▼                 ▼
                                                      ┌─────────────┐   ┌─────────────────────────────────┐
                                                      │ UK Payment  │   │     European Payment Types      │
                                                      │    Types    │   ├─────────────┬─────────────┬─────────────┐
                                                      ├─────────────┤   │             │             │             │
                                                      │             │   │             │             │             │
                                                      ▼             │   ▼             ▼             ▼             │
                                                ┌─────────────┐     │ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
                                                │  UK FPS     │     │ │   TARGET2   │ │    TIPS     │ │  EBA STEP2  │
                                                │  Payment    │     │ │   Payment   │ │   Payment   │ │   Payment   │
                                                │  Service    │     │ │   Service   │ │   Service   │ │   Service   │
                                                └─────────────┘     │ └─────────────┘ └─────────────┘ └─────────────┘
                                                                    │
                                                                    ▼
                                                              ┌─────────────┐
                                                              │  UK BACS    │
                                                              │  Payment    │
                                                              │  Service    │
                                                              └─────────────┘
                                                                    │
                                                                    ▼
                                                              ┌─────────────┐
                                                              │  UK CHAPS   │
                                                              │  Payment    │
                                                              │  Service    │
                                                              └─────────────┘
```

Each service provides methods for simulating, executing, cancelling, and scheduling payments. The services are implemented using the strategy pattern, delegating to the appropriate provider based on the payment type.

### Provider Interfaces

Provider interfaces define the contracts that payment providers must implement. All payment provider interfaces extend the `BasePaymentProvider` interface, which includes common operations like SCA and health checks:

```java
public interface BasePaymentProvider {
    Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId);
    Mono<ScaResultDTO> validateSca(ScaDTO sca);
    Mono<Boolean> isHealthy();
}
```

The specific payment provider interfaces then add their own payment-specific operations:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                             Provider Interfaces                                │
├───────────────────────────────────────────────────────────────────────────────┘
│
▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                          Payment Provider Interface                           │
│                                                                               │
│   - simulate(request): Mono<PaymentSimulationResultDTO>                       │
│   - execute(request): Mono<PaymentExecutionResultDTO>                         │
│   - cancel(request): Mono<PaymentCancellationResultDTO>                       │
│   - simulateCancellation(request): Mono<PaymentSimulationResultDTO>           │
│   - schedule(request, executionDate): Mono<PaymentScheduleResultDTO>          │
│   - triggerSca(recipient, method, reference): Mono<ScaResultDTO>              │
│   - validateSca(sca): Mono<ScaResultDTO>                                      │
│   - isHealthy(): Mono<Boolean>                                                │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ├─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┐
       │                 │                 │                 │                 │                 │                 │
       ▼                 ▼                 ▼                 ▼                 ▼                 ▼                 │
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│    SEPA     │ │    SWIFT    │ │     ACH     │ │     UK      │ │  European   │ │  Internal   │
│   Payment   │ │   Payment   │ │   Payment   │ │   Payment   │ │   Payment   │ │  Transfer   │
│   Provider  │ │   Provider  │ │   Provider  │ │   Provider  │ │   Provider  │ │   Provider  │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
                                                                                       │
                                                                                       │
                                                                                       ▼
                                                                              ┌─────────────────────────────────┐
                                                                              │   European Provider Types       │
                                                                              ├─────────────┬─────────────┬─────────────┐
                                                                              │             │             │             │
                                                                              ▼             ▼             ▼             │
                                                                        ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
                                                                        │   TARGET2   │ │    TIPS     │ │  EBA STEP2  │
                                                                        │   Payment   │ │   Payment   │ │   Payment   │
                                                                        │   Provider  │ │   Provider  │ │   Provider  │
                                                                        └─────────────┘ └─────────────┘ └─────────────┘
```

These interfaces allow the system to interact with different payment providers in a consistent way. Each provider interface defines methods for simulating, executing, cancelling, and scheduling payments specific to that payment type.

### Provider Registry

The Provider Registry is a key component that implements the hexagonal architecture pattern by dynamically discovering and registering available payment providers at runtime:

```java
@Component
public class PaymentProviderRegistry {
    private final Map<PaymentProviderType, Object> providerMap = new ConcurrentHashMap<>();
    private final Map<PaymentType, PaymentProviderType> paymentTypeToProviderMap = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    // Constructor, initialization methods, and provider lookup methods...
}
```

The Provider Registry:
1. Discovers provider implementations at runtime using Spring's ApplicationContext
2. Maps payment types to appropriate providers
3. Provides methods to look up the right provider for a given payment type
4. Handles fallback to default providers when specific providers are not available

### Controllers

REST controllers expose the payment operations as HTTP endpoints:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                                Controllers                                    │
├───────────────────────────────────────────────────────────────────────────────┘
│
▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                               REST Controllers                                │
│                                                                               │
│   Common Endpoints Pattern:                                                   │
│   - POST /api/v1/payments/{type}/simulate - Simulate a payment                 │
│   - POST /api/v1/payments/{type}/execute - Execute a payment                   │
│   - POST /api/v1/payments/{type}/cancel - Cancel a payment                     │
│   - POST /api/v1/payments/{type}/schedule - Schedule a payment                 │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ├─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┐
       │                 │                 │                 │                 │                 │                 │
       ▼                 ▼                 ▼                 ▼                 ▼                 ▼                 │
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│    SEPA     │ │    SWIFT    │ │     ACH     │ │     UK      │ │  European   │ │  Internal   │
│   Payment   │ │   Payment   │ │   Payment   │ │   Payment   │ │   Payment   │ │  Transfer   │
│ Controller  │ │ Controller  │ │ Controller  │ │ Controller  │ │ Controller  │ │ Controller  │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
       │                 │                 │                 │                 │                 │
       ▼                 ▼                 ▼                 ▼                 ▼                 ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│    SEPA     │ │    SWIFT    │ │     ACH     │ │     UK      │ │  European   │ │  Internal   │
│   Payment   │ │   Payment   │ │   Payment   │ │   Payment   │ │   Payment   │ │  Transfer   │
│   Service   │ │   Service   │ │   Service   │ │   Service   │ │   Service   │ │   Service   │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

Each controller is responsible for handling HTTP requests for a specific payment type, validating input, delegating to the appropriate service, and formatting responses. Controllers are annotated with OpenAPI/Swagger annotations for comprehensive API documentation.

## Data Flow

The data flow through the system follows a consistent pattern:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                                 Data Flow                                     │
├───────────────────────────────────────────────────────────────────────────────┘
│
▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                             1. Request Reception                              │
│   Controller receives HTTP request with payment details                       │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                             2. Input Validation                               │
│   Request is validated for correctness using Bean Validation                  │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                          3. DTO to Domain Mapping                             │
│   Request DTO is mapped to domain objects using mappers                       │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            4. SCA Verification                                │
│   If SCA is included, verify authentication credentials                       │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            5. Service Processing                              │
│   Appropriate service processes the request with business logic               │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            6. Provider Selection                              │
│   Service selects appropriate provider using Provider Registry                │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            7. Provider Execution                              │
│   Provider executes the payment operation with external systems               │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            8. Result Processing                               │
│   Result is processed and mapped back to a DTO                                │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            9. Response Formation                              │
│   Controller forms and returns the HTTP response                              │
└───────────────────────────────────────────────────────────────────────────────┘
```

This consistent flow ensures that each request is handled in a predictable and maintainable way. The addition of SCA verification as a distinct step ensures that payment security requirements are consistently applied across all payment types.

## Cross-Cutting Concerns

### Security

The payment hub implements a comprehensive security model:

- **Authentication**: JWT-based authentication for API access
- **Authorization**: Role-based access control for different operations
- **Input Validation**: Thorough validation of all input data
- **Output Sanitization**: Proper encoding of output data
- **Secure Communication**: TLS for all API communication
- **Sensitive Data Handling**: Encryption and masking of sensitive data
- **Audit Logging**: Comprehensive logging of security-relevant events

### Strong Customer Authentication (SCA)

The payment hub implements Strong Customer Authentication (SCA) as required by regulations such as PSD2:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                      Strong Customer Authentication Flow                       │
├───────────────────────────────────────────────────────────────────────────────┘
│
▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                             Payment Request                                    │
│                                                                               │
│  ┌─────────────────────────────┐                                              │
│  │        ScaDTO              │                                              │
│  │                            │                                              │
│  │ - method: SMS/EMAIL/APP    │                                              │
│  │ - recipient: Phone/Email   │                                              │
│  │ - authenticationCode       │                                              │
│  │ - challengeId              │                                              │
│  └─────────────────────────────┘                                              │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                             SCA Verification                                   │
│                                                                               │
│  1. Check if SCA is required based on:                                        │
│     - Payment amount                                                          │
│     - Payment type                                                            │
│     - Risk assessment                                                         │
│                                                                               │
│  2. If required, validate SCA credentials                                     │
│                                                                               │
│  3. Return SCA result                                                         │
└───────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                             Payment Processing                                │
│                                                                               │
│  ┌─────────────────────────────┐                                              │
│  │      ScaResultDTO          │                                              │
│  │                            │                                              │
│  │ - success: boolean         │                                              │
│  │ - method: SMS/EMAIL/APP    │                                              │
│  │ - challengeId              │                                              │
│  │ - verificationTimestamp    │                                              │
│  │ - errorCode/errorMessage   │                                              │
│  └─────────────────────────────┘                                              │
│                                                                               │
│  If SCA successful → Process payment                                          │
│  If SCA failed → Reject payment with appropriate error                        │
└───────────────────────────────────────────────────────────────────────────────┘
```

SCA is integrated into all payment operations that require it:

- **Payment Simulation**: SCA requirements are checked and indicated in the result
- **Payment Execution**: SCA is validated before executing the payment
- **Payment Cancellation**: SCA may be required for cancelling certain payments
- **Payment Scheduling**: SCA is validated during the scheduling process

### Error Handling

The payment hub implements a consistent error handling strategy:

- **Global Exception Handling**: Centralized handling of exceptions
- **Structured Error Responses**: Consistent error response format
- **Detailed Error Information**: Sufficient information for troubleshooting
- **Error Categorization**: Classification of errors by type and severity
- **Error Logging**: Comprehensive logging of errors for analysis

### Logging and Monitoring

The payment hub provides comprehensive logging and monitoring:

- **Structured Logging**: JSON-formatted logs with consistent fields
- **Correlation IDs**: Tracking of requests across components
- **Performance Metrics**: Measurement of key performance indicators
- **Health Checks**: Endpoints for system health monitoring
- **Alerting**: Notification of critical issues

### Validation

The payment hub implements thorough validation at multiple levels:

- **API Validation**: Validation of HTTP requests using Bean Validation
- **Business Validation**: Validation of business rules in services
- **Cross-Field Validation**: Validation of relationships between fields
- **Provider-Specific Validation**: Validation specific to each provider

## Deployment Architecture

The payment hub is designed to be deployed in various environments:

### Development Environment

- Single instance deployment
- In-memory or containerized databases
- Mock external services
- Comprehensive logging

### Testing Environment

- Multiple instance deployment
- Shared databases
- Test versions of external services
- Performance and load testing capabilities

### Production Environment

- Horizontally scaled deployment
- High-availability database configuration
- Production external service connections
- Monitoring and alerting

### Containerization

The payment hub is containerized using Docker:

- Separate containers for each module
- Configuration via environment variables
- Health check endpoints
- Resource limits and requests

### Orchestration

The payment hub can be orchestrated using Kubernetes:

- Deployment manifests for each component
- Horizontal Pod Autoscaling
- Service discovery and load balancing
- ConfigMaps and Secrets for configuration
- Persistent volume claims for stateful components

## Integration Patterns

The payment hub implements various integration patterns:

### Synchronous Integration

- REST APIs for direct integration
- Request-response pattern
- Timeout handling
- Circuit breaking for fault tolerance

### Asynchronous Integration

- Message-based integration for non-blocking operations
- Publish-subscribe pattern for event notification
- Message queuing for reliable delivery
- Idempotent processing for reliability

### Batch Integration

- File-based integration for batch processing
- Scheduled processing
- Checkpointing for reliability
- Comprehensive reporting

## Performance Considerations

The payment hub is designed with performance in mind:

- **Reactive Programming**: Non-blocking I/O for high throughput
- **Connection Pooling**: Efficient use of database connections
- **Caching**: Caching of frequently accessed data
- **Asynchronous Processing**: Parallel processing where possible
- **Batch Processing**: Efficient handling of bulk operations
- **Database Optimization**: Proper indexing and query optimization
- **Resource Management**: Careful management of system resources

## Scalability Considerations

The payment hub is designed to scale as needed:

- **Statelessness**: No server-side state for horizontal scaling
- **Database Scaling**: Support for database clustering and sharding
- **Load Balancing**: Distribution of requests across instances
- **Caching Strategies**: Distributed caching for scalability
- **Asynchronous Processing**: Decoupling for independent scaling
- **Microservices Approach**: Independent scaling of components

## Security Considerations

The payment hub prioritizes security:

- **Defense in Depth**: Multiple layers of security controls
- **Principle of Least Privilege**: Minimal access rights
- **Secure by Default**: Secure default configurations
- **Regular Security Testing**: Penetration testing and vulnerability scanning
- **Compliance**: Adherence to industry standards and regulations
- **Security Monitoring**: Detection of security events
- **Incident Response**: Procedures for handling security incidents

## Conclusion

The Firefly Core Banking Payment Hub architecture provides a solid foundation for a flexible, scalable, and maintainable payment processing system. By following the hexagonal architecture pattern and implementing industry best practices, the system can adapt to changing requirements while maintaining high quality and performance.

The modular structure, clear separation of concerns, and extensible design allow for easy addition of new payment types and providers, ensuring that the system can evolve with the changing payment landscape.

## References

1. Cockburn, A. - [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
2. Evans, E. - [Domain-Driven Design](https://domainlanguage.com/ddd/)
3. Fowler, M. - [Patterns of Enterprise Application Architecture](https://martinfowler.com/books/eaa.html)
4. Newman, S. - [Building Microservices](https://samnewman.io/books/building_microservices/)
5. Vernon, V. - [Implementing Domain-Driven Design](https://vaughnvernon.co/?page_id=168)
