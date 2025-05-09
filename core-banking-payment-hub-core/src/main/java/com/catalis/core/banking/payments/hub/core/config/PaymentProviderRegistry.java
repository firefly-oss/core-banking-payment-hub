package com.catalis.core.banking.payments.hub.core.config;

import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentType;
import com.catalis.core.banking.payments.hub.interfaces.providers.AchPaymentProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.EbaStep2PaymentProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.InternalTransferProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.SepaPaymentProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.SwiftPaymentProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.Target2PaymentProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.TipsPaymentProvider;
import com.catalis.core.banking.payments.hub.interfaces.providers.UkPaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for payment providers that automatically discovers and registers available providers.
 * This implements the hexagonal architecture pattern by allowing the core application to
 * dynamically discover and use provider implementations without direct dependencies.
 */
@Component
public class PaymentProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(PaymentProviderRegistry.class);

    private final ApplicationContext applicationContext;
    private final Map<PaymentProviderType, Object> providerMap = new EnumMap<>(PaymentProviderType.class);
    private final Map<PaymentType, PaymentProviderType> paymentTypeToProviderMap = new EnumMap<>(PaymentType.class);

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

    private void discoverProviders() {
        // Discover SEPA providers
        Map<String, SepaPaymentProvider> sepaProviders = applicationContext.getBeansOfType(SepaPaymentProvider.class);
        if (!sepaProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.SEPA_PROVIDER, sepaProviders.values().iterator().next());
            log.info("Registered SEPA payment provider: {}", sepaProviders.values().iterator().next().getClass().getName());
        }

        // Discover SWIFT providers
        Map<String, SwiftPaymentProvider> swiftProviders = applicationContext.getBeansOfType(SwiftPaymentProvider.class);
        if (!swiftProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.SWIFT_PROVIDER, swiftProviders.values().iterator().next());
            log.info("Registered SWIFT payment provider: {}", swiftProviders.values().iterator().next().getClass().getName());
        }

        // Discover ACH providers
        Map<String, AchPaymentProvider> achProviders = applicationContext.getBeansOfType(AchPaymentProvider.class);
        if (!achProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.ACH_PROVIDER, achProviders.values().iterator().next());
            log.info("Registered ACH payment provider: {}", achProviders.values().iterator().next().getClass().getName());
        }

        // Discover UK providers
        Map<String, UkPaymentProvider> ukProviders = applicationContext.getBeansOfType(UkPaymentProvider.class);
        if (!ukProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.UK_PROVIDER, ukProviders.values().iterator().next());
            log.info("Registered UK payment provider: {}", ukProviders.values().iterator().next().getClass().getName());
        }

        // Discover TARGET2 providers
        Map<String, Target2PaymentProvider> target2Providers = applicationContext.getBeansOfType(Target2PaymentProvider.class);
        if (!target2Providers.isEmpty()) {
            providerMap.put(PaymentProviderType.TARGET2_PROVIDER, target2Providers.values().iterator().next());
            log.info("Registered TARGET2 payment provider: {}", target2Providers.values().iterator().next().getClass().getName());
        }

        // Discover TIPS providers
        Map<String, TipsPaymentProvider> tipsProviders = applicationContext.getBeansOfType(TipsPaymentProvider.class);
        if (!tipsProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.TIPS_PROVIDER, tipsProviders.values().iterator().next());
            log.info("Registered TIPS payment provider: {}", tipsProviders.values().iterator().next().getClass().getName());
        }

        // Discover EBA STEP2 providers
        Map<String, EbaStep2PaymentProvider> ebaStep2Providers = applicationContext.getBeansOfType(EbaStep2PaymentProvider.class);
        if (!ebaStep2Providers.isEmpty()) {
            providerMap.put(PaymentProviderType.EBA_STEP2_PROVIDER, ebaStep2Providers.values().iterator().next());
            log.info("Registered EBA STEP2 payment provider: {}", ebaStep2Providers.values().iterator().next().getClass().getName());
        }

        // Discover Internal Transfer providers
        Map<String, InternalTransferProvider> internalProviders = applicationContext.getBeansOfType(InternalTransferProvider.class);
        if (!internalProviders.isEmpty()) {
            providerMap.put(PaymentProviderType.INTERNAL_PROVIDER, internalProviders.values().iterator().next());
            log.info("Registered Internal Transfer provider: {}", internalProviders.values().iterator().next().getClass().getName());
        }

        // Set default provider if available
        if (!providerMap.isEmpty()) {
            providerMap.put(PaymentProviderType.DEFAULT_PROVIDER, providerMap.values().iterator().next());
        }
    }

    private void mapPaymentTypesToProviders() {
        // Map SEPA payment types to SEPA provider
        paymentTypeToProviderMap.put(PaymentType.SEPA_SCT, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_ICT, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SDD, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SDD_CORE, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SDD_B2B, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SCT_INST, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SDD_RECURRENT, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SDD_ONE_OFF, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SCT_FUTURE_DATED, PaymentProviderType.SEPA_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SEPA_SCT_STANDING_ORDER, PaymentProviderType.SEPA_PROVIDER);

        // Map SWIFT payment types to SWIFT provider
        paymentTypeToProviderMap.put(PaymentType.SWIFT_MT103, PaymentProviderType.SWIFT_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SWIFT_MT202, PaymentProviderType.SWIFT_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.SWIFT_PACS_008, PaymentProviderType.SWIFT_PROVIDER);

        // Map ACH payment types to ACH provider
        paymentTypeToProviderMap.put(PaymentType.ACH_CREDIT, PaymentProviderType.ACH_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.ACH_DEBIT, PaymentProviderType.ACH_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.WIRE_TRANSFER, PaymentProviderType.ACH_PROVIDER);

        // Map UK payment types to UK provider
        paymentTypeToProviderMap.put(PaymentType.UK_FPS, PaymentProviderType.UK_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.UK_BACS, PaymentProviderType.UK_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.UK_BACS_DIRECT_DEBIT, PaymentProviderType.UK_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.UK_CHAPS, PaymentProviderType.UK_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.UK_STANDING_ORDER, PaymentProviderType.UK_PROVIDER);

        // Map European payment types to their respective providers
        paymentTypeToProviderMap.put(PaymentType.TARGET2, PaymentProviderType.TARGET2_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.TIPS, PaymentProviderType.TIPS_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.EBA_STEP2, PaymentProviderType.EBA_STEP2_PROVIDER);

        // Map internal payment types to internal provider
        paymentTypeToProviderMap.put(PaymentType.INTERNAL_TRANSFER, PaymentProviderType.INTERNAL_PROVIDER);
        paymentTypeToProviderMap.put(PaymentType.INTERNAL_BULK_TRANSFER, PaymentProviderType.INTERNAL_PROVIDER);
    }

    private void logAvailableProviders() {
        log.info("Payment Provider Registry initialized with {} providers", providerMap.size());
        providerMap.forEach((type, provider) ->
            log.info("Provider type: {}, Implementation: {}", type, provider.getClass().getName()));
    }

    /**
     * Gets the appropriate provider for the given payment type.
     *
     * @param paymentType The payment type
     * @param <T> The provider type
     * @return The provider instance, or empty if no provider is available
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProviderForPaymentType(PaymentType paymentType) {
        PaymentProviderType providerType = paymentTypeToProviderMap.getOrDefault(paymentType, PaymentProviderType.DEFAULT_PROVIDER);
        return Optional.ofNullable((T) providerMap.get(providerType));
    }

    /**
     * Gets the provider of the specified type.
     *
     * @param providerType The provider type
     * @param <T> The provider interface type
     * @return The provider instance, or empty if no provider is available
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProvider(PaymentProviderType providerType) {
        return Optional.ofNullable((T) providerMap.get(providerType));
    }

    /**
     * Gets the default provider.
     *
     * @param <T> The provider interface type
     * @return The default provider instance, or empty if no provider is available
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getDefaultProvider() {
        return Optional.ofNullable((T) providerMap.get(PaymentProviderType.DEFAULT_PROVIDER));
    }
}
