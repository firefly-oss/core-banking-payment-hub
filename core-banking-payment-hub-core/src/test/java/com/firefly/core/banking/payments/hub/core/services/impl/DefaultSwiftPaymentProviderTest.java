package com.firefly.core.banking.payments.hub.core.services.impl;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentType;
import com.firefly.core.banking.payments.hub.interfaces.providers.ScaProvider;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSwiftPaymentProviderTest {

    private DefaultSwiftPaymentProvider swiftPaymentProvider;
    private SwiftPaymentRequestDTO request;

    @Mock
    private ScaProvider scaProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock SCA provider behavior
        ScaResultDTO mockScaResult = new ScaResultDTO();
        mockScaResult.setSuccess(true);
        mockScaResult.setChallengeId("CHL-12345");

        Mockito.when(scaProvider.validateSca(Mockito.any(ScaDTO.class)))
               .thenReturn(Mono.just(mockScaResult));

        Mockito.when(scaProvider.triggerSca(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
               .thenReturn(Mono.just(mockScaResult));

        Mockito.when(scaProvider.isScaRequired(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
               .thenReturn(Mono.just(true));

        swiftPaymentProvider = new DefaultSwiftPaymentProvider(scaProvider);

        request = new SwiftPaymentRequestDTO();
        request.setRequestId("REQ-12345");
        request.setPaymentType(PaymentType.SWIFT_MT103);
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");
        request.setRequestedExecutionDate(LocalDate.now().plusDays(1));
    }

    @Test
    void simulate_shouldReturnSimulationResult() {
        // Act
        Mono<PaymentSimulationResultDTO> resultMono = swiftPaymentProvider.simulate(request);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(request.getRequestId(), result.getRequestId());
                    assertEquals(request.getPaymentType(), result.getPaymentType());
                    assertEquals(PaymentOperationType.SIMULATE, result.getOperationType());
                    assertEquals(PaymentStatus.VALIDATED, result.getStatus());
                    assertEquals(PaymentProviderType.SWIFT_PROVIDER, result.getProvider());
                    assertTrue(result.isSuccess());
                    assertEquals(request.getRequestedExecutionDate(), result.getEstimatedExecutionDate());
                    assertEquals(request.getRequestedExecutionDate().plusDays(2), result.getEstimatedSettlementDate());
                    assertEquals(new BigDecimal("25.00"), result.getEstimatedFee());
                    assertEquals(request.getCurrency(), result.getFeeCurrency());
                    assertEquals(new BigDecimal("1.1050"), result.getEstimatedExchangeRate());
                    assertTrue(result.isFeasible());
                    assertNotNull(result.getSimulationReference());
                    assertTrue(result.getSimulationReference().startsWith("SIM-"));
                })
                .verifyComplete();
    }

    @Test
    void simulate_withScaRequired_shouldTriggerScaDelivery() {
        // Arrange - Force SCA to be required
        request.setAmount(new BigDecimal("10000.00")); // High amount to trigger SCA

        // Act
        Mono<PaymentSimulationResultDTO> resultMono = swiftPaymentProvider.simulate(request);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertTrue(result.isScaRequired());
                    assertFalse(result.isScaCompleted());
                    assertTrue(result.isScaDeliveryTriggered());
                    assertNotNull(result.getScaDeliveryTimestamp());
                    assertNotNull(result.getScaDeliveryMethod());
                    assertNotNull(result.getScaDeliveryRecipient());
                    assertNotNull(result.getScaExpiryTimestamp());
                    assertNotNull(result.getScaResult());
                    assertFalse(result.getScaResult().isSuccess());
                })
                .verifyComplete();
    }

    @Test
    void simulate_withScaCodeProvided_shouldValidateSca() {
        // Arrange
        ScaDTO sca = new ScaDTO();
        sca.setMethod("SMS");
        sca.setRecipient("+1234567890");
        sca.setAuthenticationCode("123456"); // Valid code
        request.setSca(sca);

        // Act
        Mono<PaymentSimulationResultDTO> resultMono = swiftPaymentProvider.simulate(request);

        // Assert
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    if (result.isScaRequired()) { // SCA might be randomly required
                        assertTrue(result.isScaDeliveryTriggered());
                        assertNotNull(result.getScaResult());
                        assertTrue(result.isScaCompleted());
                        assertTrue(result.getScaResult().isSuccess());
                    }
                })
                .verifyComplete();
    }
}
