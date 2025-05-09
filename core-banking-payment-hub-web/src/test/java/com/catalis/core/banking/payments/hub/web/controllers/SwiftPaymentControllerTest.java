package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.SwiftPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.AccountInfoDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PartyInfoDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.enums.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SwiftPaymentControllerTest {

    @Mock
    private SwiftPaymentService swiftPaymentService;

    @InjectMocks
    private SwiftPaymentController swiftPaymentController;

    private SwiftPaymentRequestDTO paymentRequest;
    private SwiftPaymentRequestDTO paymentRequestWithSca;

    @BeforeEach
    void setUp() {
        // Create a basic payment request
        paymentRequest = SwiftPaymentRequestDTO.builder()
                .paymentType(PaymentType.SWIFT_MT103)
                .amount(new BigDecimal("2000.00"))
                .currency("USD")
                .orderingCustomer(PartyInfoDTO.builder()
                        .name("John Doe")
                        .address("123 Main St")
                        .countryCode("US")
                        .build())
                .orderingInstitution(AccountInfoDTO.builder()
                        .bic("DEUTDEFF")
                        .bankName("Deutsche Bank")
                        .build())
                .beneficiaryCustomer(PartyInfoDTO.builder()
                        .name("Jane Smith")
                        .address("456 Oak St")
                        .countryCode("GB")
                        .build())
                .beneficiaryInstitution(AccountInfoDTO.builder()
                        .bic("BARCGB22")
                        .bankName("Barclays Bank")
                        .bankCountryCode("GB")
                        .build())
                .paymentDetails("Invoice payment #12345")
                .build();

        // Create a payment request with SCA
        paymentRequestWithSca = SwiftPaymentRequestDTO.builder()
                .paymentType(PaymentType.SWIFT_MT103)
                .amount(new BigDecimal("2000.00"))
                .currency("USD")
                .orderingCustomer(PartyInfoDTO.builder()
                        .name("John Doe")
                        .address("123 Main St")
                        .countryCode("US")
                        .build())
                .orderingInstitution(AccountInfoDTO.builder()
                        .bic("DEUTDEFF")
                        .bankName("Deutsche Bank")
                        .build())
                .beneficiaryCustomer(PartyInfoDTO.builder()
                        .name("Jane Smith")
                        .address("456 Oak St")
                        .countryCode("GB")
                        .build())
                .beneficiaryInstitution(AccountInfoDTO.builder()
                        .bic("BARCGB22")
                        .bankName("Barclays Bank")
                        .bankCountryCode("GB")
                        .build())
                .paymentDetails("Invoice payment #12345")
                .sca(ScaDTO.builder()
                        .method("SMS")
                        .recipient("+34600000000")
                        .authenticationCode("123456")
                        .build())
                .build();
    }

    @Test
    void testSimulatePaymentWithoutSca() {
        // Mock the service response
        PaymentSimulationResultDTO mockResult = PaymentSimulationResultDTO.builder()
                .paymentId("PAY-12345678")
                .success(true)
                .scaRequired(true)
                .scaCompleted(false)
                .build();

        when(swiftPaymentService.simulatePayment(any(SwiftPaymentRequestDTO.class)))
                .thenReturn(Mono.just(mockResult));

        // Test the controller
        StepVerifier.create(swiftPaymentController.simulatePayment(paymentRequest))
                .expectNextMatches(result -> 
                        result.isSuccess() && 
                        result.isScaRequired() && 
                        !result.isScaCompleted())
                .verifyComplete();
    }

    @Test
    void testSimulatePaymentWithSca() {
        // Mock the service response
        PaymentSimulationResultDTO mockResult = PaymentSimulationResultDTO.builder()
                .paymentId("PAY-12345678")
                .success(true)
                .scaRequired(true)
                .scaCompleted(true)
                .build();

        when(swiftPaymentService.simulatePayment(any(SwiftPaymentRequestDTO.class)))
                .thenReturn(Mono.just(mockResult));

        // Test the controller
        StepVerifier.create(swiftPaymentController.simulatePayment(paymentRequestWithSca))
                .expectNextMatches(result -> 
                        result.isSuccess() && 
                        result.isScaRequired() && 
                        result.isScaCompleted())
                .verifyComplete();
    }

    @Test
    void testExecutePaymentWithSca() {
        // Mock the service response
        PaymentExecutionResultDTO mockResult = PaymentExecutionResultDTO.builder()
                .paymentId("PAY-12345678")
                .success(true)
                .scaRequired(true)
                .scaCompleted(true)
                .build();

        when(swiftPaymentService.executePayment(any(SwiftPaymentRequestDTO.class)))
                .thenReturn(Mono.just(mockResult));

        // Test the controller
        StepVerifier.create(swiftPaymentController.executePayment(paymentRequestWithSca))
                .expectNextMatches(result -> 
                        result.isSuccess() && 
                        result.isScaRequired() && 
                        result.isScaCompleted())
                .verifyComplete();
    }

    @Test
    void testExecutePaymentWithoutRequiredSca() {
        // Mock the service response for a rejected payment due to missing SCA
        PaymentExecutionResultDTO mockResult = PaymentExecutionResultDTO.builder()
                .paymentId("PAY-12345678")
                .success(false)
                .scaRequired(true)
                .scaCompleted(false)
                .errorCode("SCA_REQUIRED")
                .errorMessage("Strong Customer Authentication is required for this payment")
                .build();

        when(swiftPaymentService.executePayment(any(SwiftPaymentRequestDTO.class)))
                .thenReturn(Mono.just(mockResult));

        // Test the controller
        StepVerifier.create(swiftPaymentController.executePayment(paymentRequest))
                .expectNextMatches(result -> 
                        !result.isSuccess() && 
                        result.isScaRequired() && 
                        !result.isScaCompleted() &&
                        "SCA_REQUIRED".equals(result.getErrorCode()))
                .verifyComplete();
    }
}
