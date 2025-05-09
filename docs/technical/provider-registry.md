# Provider Registry

## Overview

The Provider Registry is a central component of the Firefly Core Banking Payment Hub that implements the hexagonal architecture pattern by dynamically discovering and registering available payment providers at runtime. This document provides a detailed explanation of the Provider Registry's design, implementation, and usage.

## Purpose and Responsibilities

The Provider Registry serves several key purposes:

1. **Dynamic Provider Discovery**: Automatically discovers payment provider implementations at runtime
2. **Provider Registration**: Registers discovered providers for use by the system
3. **Payment Type Mapping**: Maps payment types to appropriate provider types
4. **Provider Selection**: Selects the appropriate provider for a given payment operation
5. **Fallback Handling**: Provides fallback mechanisms when specific providers are not available

These responsibilities enable the payment hub to be highly extensible and adaptable to different deployment scenarios without requiring code changes.

## Design Principles

The Provider Registry is designed according to the following principles:

### Loose Coupling

The registry decouples payment services from specific provider implementations, allowing them to be developed and evolved independently.

### Open/Closed Principle

The system is open for extension (new providers can be added) but closed for modification (existing code doesn't need to change).

### Dependency Inversion

High-level components (services) depend on abstractions (provider interfaces), not concrete implementations.

### Single Responsibility

The registry has a single responsibility: managing the lifecycle and selection of payment providers.

### Configuration over Code

Provider selection and mapping is driven by configuration rather than hardcoded logic.

## Implementation

### Core Components

The Provider Registry consists of the following core components:

#### PaymentProviderRegistry Class

The main class that manages provider discovery, registration, and lookup:

```java
@Component
public class PaymentProviderRegistry {
    private static final Logger log = LoggerFactory.getLogger(PaymentProviderRegistry.class);
    
    private final Map<PaymentProviderType, Object> providerMap = new ConcurrentHashMap<>();
    private final Map<PaymentType, PaymentProviderType> paymentTypeToProviderMap = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;
    
    @Autowired
    public PaymentProviderRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct
    public void initialize() {
        discoverProviders();
        mapPaymentTypesToProviders();
        logAvailableProviders();
    }
    
    // Provider discovery, mapping, and lookup methods...
}
```

#### PaymentProviderType Enum

An enumeration of the different types of payment providers:

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
    
    // Constructor, getters, etc...
}
```

#### PaymentType Enum

An enumeration of the different payment types supported by the system:

```java
public enum PaymentType {
    // SEPA payment types
    SEPA_SCT("SEPA Credit Transfer"),
    SEPA_ICT("SEPA Instant Credit Transfer"),
    SEPA_SDD("SEPA Direct Debit"),
    // ... other payment types
    
    private final String description;
    
    // Constructor, getters, etc...
}
```

### Provider Discovery

The Provider Registry discovers provider implementations at runtime using Spring's ApplicationContext:

```java
private void discoverProviders() {
    // Discover SEPA providers
    Map<String, SepaPaymentProvider> sepaProviders = 
        applicationContext.getBeansOfType(SepaPaymentProvider.class);
    if (!sepaProviders.isEmpty()) {
        providerMap.put(PaymentProviderType.SEPA_PROVIDER, 
            sepaProviders.values().iterator().next());
        log.info("Registered SEPA payment provider: {}", 
            sepaProviders.values().iterator().next().getClass().getName());
    }
    
    // Discover SWIFT providers
    Map<String, SwiftPaymentProvider> swiftProviders = 
        applicationContext.getBeansOfType(SwiftPaymentProvider.class);
    if (!swiftProviders.isEmpty()) {
        providerMap.put(PaymentProviderType.SWIFT_PROVIDER, 
            swiftProviders.values().iterator().next());
        log.info("Registered SWIFT payment provider: {}", 
            swiftProviders.values().iterator().next().getClass().getName());
    }
    
    // Similar code for other provider types...
}
```

This approach allows the system to automatically discover and register provider implementations that are available in the Spring application context, without requiring explicit configuration.

### Payment Type Mapping

The Provider Registry maps payment types to provider types:

```java
private void mapPaymentTypesToProviders() {
    // Map SEPA payment types to SEPA provider
    paymentTypeToProviderMap.put(PaymentType.SEPA_SCT, PaymentProviderType.SEPA_PROVIDER);
    paymentTypeToProviderMap.put(PaymentType.SEPA_ICT, PaymentProviderType.SEPA_PROVIDER);
    paymentTypeToProviderMap.put(PaymentType.SEPA_SDD, PaymentProviderType.SEPA_PROVIDER);
    // ... other SEPA payment types
    
    // Map SWIFT payment types to SWIFT provider
    paymentTypeToProviderMap.put(PaymentType.SWIFT_MT103, PaymentProviderType.SWIFT_PROVIDER);
    paymentTypeToProviderMap.put(PaymentType.SWIFT_MT202, PaymentProviderType.SWIFT_PROVIDER);
    paymentTypeToProviderMap.put(PaymentType.SWIFT_PACS_008, PaymentProviderType.SWIFT_PROVIDER);
    
    // Similar mappings for other payment types...
}
```

This mapping allows the system to determine which provider type should handle a specific payment type.

### Provider Lookup

The Provider Registry provides methods to look up the appropriate provider for a given payment type or provider type:

```java
public <T> Optional<T> getProvider(PaymentProviderType providerType) {
    if (providerMap.containsKey(providerType)) {
        try {
            @SuppressWarnings("unchecked")
            T provider = (T) providerMap.get(providerType);
            return Optional.of(provider);
        } catch (ClassCastException e) {
            log.error("Provider type mismatch for {}: {}", providerType, e.getMessage());
            return Optional.empty();
        }
    }
    return Optional.empty();
}

public <T> Optional<T> getProviderForPaymentType(PaymentType paymentType) {
    PaymentProviderType providerType = paymentTypeToProviderMap.get(paymentType);
    if (providerType != null) {
        return getProvider(providerType);
    }
    log.warn("No provider type mapped for payment type: {}", paymentType);
    return Optional.empty();
}
```

These methods allow services to retrieve the appropriate provider for a given payment operation without knowing the specific implementation details.

## Usage in Services

Payment services use the Provider Registry to obtain the appropriate provider for a given payment operation:

```java
@Service
public class SepaPaymentServiceImpl implements SepaPaymentService {
    private final PaymentProviderRegistry providerRegistry;
    
    @Autowired
    public SepaPaymentServiceImpl(PaymentProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }
    
    @Override
    public Mono<PaymentSimulationResultDTO> simulatePayment(SepaPaymentRequestDTO request) {
        return getProvider()
                .map(provider -> provider.simulate(request))
                .orElseGet(() -> Mono.error(
                    new IllegalStateException("No SEPA payment provider available")));
    }
    
    private Optional<SepaPaymentProvider> getProvider() {
        return providerRegistry.getProvider(PaymentProviderType.SEPA_PROVIDER);
    }
    
    // Other service methods...
}
```

This approach allows services to focus on orchestrating payment operations without being tightly coupled to specific provider implementations.

## Provider Implementation

To create a new payment provider, implement the appropriate provider interface and register it as a Spring component:

```java
@Component
public class DefaultSepaPaymentProvider implements SepaPaymentProvider {
    @Override
    public Mono<PaymentSimulationResultDTO> simulate(SepaPaymentRequestDTO request) {
        // Implementation...
    }
    
    @Override
    public Mono<PaymentExecutionResultDTO> execute(SepaPaymentRequestDTO request) {
        // Implementation...
    }
    
    // Other provider methods...
}
```

The Provider Registry will automatically discover and register this provider at runtime.

## Configuration

The Provider Registry can be configured through application properties:

```yaml
payment:
  providers:
    sepa:
      enabled: true
      priority: 1
    swift:
      enabled: true
      priority: 1
    ach:
      enabled: true
      priority: 1
    # Other provider configurations...
```

These properties can be used to enable or disable specific providers, or to control their priority when multiple providers of the same type are available.

## Advanced Features

### Multiple Provider Support

The Provider Registry can be extended to support multiple providers of the same type:

```java
private final Map<PaymentProviderType, List<Object>> multiProviderMap = new ConcurrentHashMap<>();

private void discoverMultipleProviders() {
    // Discover SEPA providers
    Map<String, SepaPaymentProvider> sepaProviders = 
        applicationContext.getBeansOfType(SepaPaymentProvider.class);
    if (!sepaProviders.isEmpty()) {
        multiProviderMap.put(PaymentProviderType.SEPA_PROVIDER, 
            new ArrayList<>(sepaProviders.values()));
        log.info("Registered {} SEPA payment providers", sepaProviders.size());
    }
    
    // Similar code for other provider types...
}

public <T> List<T> getAllProviders(PaymentProviderType providerType) {
    if (multiProviderMap.containsKey(providerType)) {
        try {
            @SuppressWarnings("unchecked")
            List<T> providers = (List<T>) multiProviderMap.get(providerType);
            return providers;
        } catch (ClassCastException e) {
            log.error("Provider type mismatch for {}: {}", providerType, e.getMessage());
            return Collections.emptyList();
        }
    }
    return Collections.emptyList();
}
```

This allows the system to support scenarios where multiple providers of the same type are available, such as for routing payments through different channels based on criteria like cost, speed, or availability.

### Provider Selection Strategies

The Provider Registry can be extended with provider selection strategies:

```java
public interface ProviderSelectionStrategy<T> {
    Optional<T> selectProvider(List<T> availableProviders, Object context);
}

@Component
public class CostBasedProviderSelectionStrategy<T extends PricingAware> 
        implements ProviderSelectionStrategy<T> {
    @Override
    public Optional<T> selectProvider(List<T> availableProviders, Object context) {
        if (availableProviders.isEmpty()) {
            return Optional.empty();
        }
        
        // Select the provider with the lowest cost
        return availableProviders.stream()
                .min(Comparator.comparing(T::getCost));
    }
}
```

These strategies can be used to select the most appropriate provider based on various criteria.

### Dynamic Provider Reconfiguration

The Provider Registry can support dynamic reconfiguration of providers:

```java
@Scheduled(fixedDelay = 60000) // Run every minute
public void refreshProviders() {
    log.info("Refreshing payment providers...");
    discoverProviders();
    logAvailableProviders();
}

@EventListener(ApplicationEvent.class)
public void handleProviderEvent(ProviderEvent event) {
    if (event.getType() == ProviderEventType.PROVIDER_ADDED || 
        event.getType() == ProviderEventType.PROVIDER_REMOVED) {
        log.info("Provider event detected: {}, refreshing providers...", event.getType());
        refreshProviders();
    }
}
```

This allows the system to adapt to changes in the provider landscape without requiring a restart.

## Testing

The Provider Registry can be easily tested using mock providers:

```java
@ExtendWith(MockitoExtension.class)
public class PaymentProviderRegistryTest {
    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private SepaPaymentProvider sepaPaymentProvider;
    
    @Mock
    private SwiftPaymentProvider swiftPaymentProvider;
    
    private PaymentProviderRegistry registry;
    
    @BeforeEach
    public void setup() {
        // Set up mock providers
        Map<String, SepaPaymentProvider> sepaProviders = new HashMap<>();
        sepaProviders.put("sepaProvider", sepaPaymentProvider);
        
        Map<String, SwiftPaymentProvider> swiftProviders = new HashMap<>();
        swiftProviders.put("swiftProvider", swiftPaymentProvider);
        
        // Configure ApplicationContext to return mock providers
        when(applicationContext.getBeansOfType(SepaPaymentProvider.class))
            .thenReturn(sepaProviders);
        when(applicationContext.getBeansOfType(SwiftPaymentProvider.class))
            .thenReturn(swiftProviders);
        
        // Create and initialize registry
        registry = new PaymentProviderRegistry(applicationContext);
        registry.initialize();
    }
    
    @Test
    public void testGetSepaProvider() {
        Optional<SepaPaymentProvider> provider = 
            registry.getProvider(PaymentProviderType.SEPA_PROVIDER);
        assertTrue(provider.isPresent());
        assertEquals(sepaPaymentProvider, provider.get());
    }
    
    // Other tests...
}
```

This approach allows for thorough testing of the Provider Registry's functionality without requiring actual provider implementations.

## Best Practices

### Provider Implementation

When implementing payment providers:

1. **Implement a single provider interface** per class
2. **Make providers stateless** to support horizontal scaling
3. **Handle errors gracefully** and provide meaningful error messages
4. **Log important events** for troubleshooting
5. **Validate input** before processing
6. **Implement proper security measures** for external communication
7. **Use circuit breakers** for external dependencies
8. **Implement retry logic** for transient failures
9. **Provide meaningful metrics** for monitoring
10. **Document provider-specific requirements** and limitations

### Provider Registry Usage

When using the Provider Registry:

1. **Always check for provider availability** before use
2. **Handle missing providers gracefully** with appropriate error messages
3. **Consider fallback strategies** when preferred providers are unavailable
4. **Use appropriate exception types** for different error scenarios
5. **Log provider selection** for troubleshooting
6. **Monitor provider availability** for operational visibility
7. **Consider provider performance** when selecting providers
8. **Test with different provider configurations** to ensure robustness
9. **Document provider dependencies** for deployment planning
10. **Consider provider versioning** for backward compatibility

## Conclusion

The Provider Registry is a key component of the Firefly Core Banking Payment Hub that enables the system to be highly extensible and adaptable. By dynamically discovering and registering payment providers at runtime, the registry allows the system to support a wide range of payment types and providers without requiring code changes.

The registry's design follows the principles of the hexagonal architecture pattern, providing a clear separation between the core domain logic and external systems. This separation makes the system more maintainable, testable, and adaptable to changing requirements.

## References

1. Cockburn, A. - [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
2. Fowler, M. - [Inversion of Control Containers and the Dependency Injection pattern](https://martinfowler.com/articles/injection.html)
3. Johnson, R. et al. - [Spring Framework Reference Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
4. Gamma, E. et al. - [Design Patterns: Elements of Reusable Object-Oriented Software](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/)
5. Vernon, V. - [Implementing Domain-Driven Design](https://vaughnvernon.co/?page_id=168)
