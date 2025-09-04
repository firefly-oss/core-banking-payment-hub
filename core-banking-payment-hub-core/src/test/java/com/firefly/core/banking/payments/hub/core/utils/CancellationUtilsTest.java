/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.core.banking.payments.hub.core.utils;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentOperationType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentProviderType;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentStatus;
import com.firefly.core.banking.payments.hub.interfaces.enums.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CancellationUtilsTest {

    @Test
    void createCancellationSimulationResult_shouldReturnValidResult() {
        // Arrange
        String paymentId = "PAY-12345678";
        PaymentType paymentType = PaymentType.SEPA_SCT;
        PaymentProviderType providerType = PaymentProviderType.SEPA_PROVIDER;
        String feeCurrency = "EUR";
        boolean scaRequired = true;
        boolean feasible = true;

        // Act
        PaymentSimulationResultDTO result = CancellationUtils.createCancellationSimulationResult(
                paymentId, paymentType, providerType, feeCurrency, scaRequired, feasible);

        // Assert
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(paymentType, result.getPaymentType());
        assertEquals(PaymentOperationType.SIMULATE, result.getOperationType());
        assertEquals(PaymentStatus.VALIDATED, result.getStatus());
        assertEquals(providerType, result.getProvider());
        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("5.00"), result.getEstimatedFee());
        assertEquals(feeCurrency, result.getFeeCurrency());
        assertEquals(feasible, result.isFeasible());
        assertNotNull(result.getSimulationReference());
        assertTrue(result.getSimulationReference().startsWith("SIM-"));
        assertEquals(scaRequired, result.isScaRequired());
        assertFalse(result.isScaCompleted());
    }

    @Test
    void setupScaDelivery_shouldSetupScaInformation() {
        // Arrange
        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        ScaDTO sca = new ScaDTO();
        sca.setMethod("SMS");
        sca.setRecipient("+1234567890");
        String defaultPhoneNumber = "+9876543210";

        // Act
        CancellationUtils.setupScaDelivery(result, sca, defaultPhoneNumber);

        // Assert
        assertTrue(result.isScaDeliveryTriggered());
        assertNotNull(result.getScaDeliveryTimestamp());
        assertEquals("SMS", result.getScaDeliveryMethod());
        assertEquals("+1234567890", result.getScaDeliveryRecipient());
        assertNotNull(result.getScaExpiryTimestamp());
        assertNotNull(result.getScaResult());
        assertEquals("SMS", result.getScaResult().getMethod());
        assertNotNull(result.getScaResult().getChallengeId());
        assertFalse(result.isScaCompleted());
    }

    @Test
    void setupScaDelivery_withDefaultValues_shouldUseDefaults() {
        // Arrange
        PaymentSimulationResultDTO result = new PaymentSimulationResultDTO();
        ScaDTO sca = null;
        String defaultPhoneNumber = "+9876543210";

        // Act
        CancellationUtils.setupScaDelivery(result, sca, defaultPhoneNumber);

        // Assert
        assertTrue(result.isScaDeliveryTriggered());
        assertNotNull(result.getScaDeliveryTimestamp());
        assertEquals("SMS", result.getScaDeliveryMethod());
        assertEquals("*****3210", result.getScaDeliveryRecipient());
        assertNotNull(result.getScaExpiryTimestamp());
        assertNotNull(result.getScaResult());
        assertEquals("SMS", result.getScaResult().getMethod());
        assertNotNull(result.getScaResult().getChallengeId());
        assertFalse(result.isScaCompleted());
    }

    @Test
    void createCancellationResult_shouldReturnValidResult() {
        // Arrange
        String paymentId = "PAY-12345678";
        PaymentType paymentType = PaymentType.SEPA_SCT;
        PaymentProviderType providerType = PaymentProviderType.SEPA_PROVIDER;
        String cancellationReason = "Duplicate payment";
        boolean scaRequired = true;

        // Act
        PaymentCancellationResultDTO result = CancellationUtils.createCancellationResult(
                paymentId, paymentType, providerType, cancellationReason, scaRequired);

        // Assert
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(paymentType, result.getPaymentType());
        assertEquals(PaymentOperationType.CANCEL, result.getOperationType());
        assertEquals(providerType, result.getProvider());
        assertEquals(cancellationReason, result.getCancellationReason());
        assertEquals(scaRequired, result.isScaRequired());
        assertFalse(result.isScaCompleted());
    }

    @Test
    void validateScaForCancellation_withNullSca_shouldReturnFalse() {
        // Arrange
        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setScaRequired(true);
        ScaDTO sca = null;

        // Act
        boolean validationResult = CancellationUtils.validateScaForCancellation(result, sca);

        // Assert
        assertFalse(validationResult);
        assertFalse(result.isSuccess());
        assertEquals(PaymentStatus.REJECTED, result.getStatus());
        assertEquals("SCA_REQUIRED", result.getErrorCode());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void validateScaForCancellation_withValidSca_shouldReturnTrue() {
        // Arrange
        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        result.setScaRequired(true);
        ScaDTO sca = new ScaDTO();
        sca.setMethod("SMS");
        sca.setAuthenticationCode("123456"); // Valid code

        // Act
        boolean validationResult = CancellationUtils.validateScaForCancellation(result, sca);

        // Assert
        assertTrue(validationResult);
        assertTrue(result.isScaCompleted());
        assertNotNull(result.getScaResult());
        assertTrue(result.getScaResult().isSuccess());
    }

    @Test
    void setCancellationNotSupported_shouldSetCorrectErrorInformation() {
        // Arrange
        PaymentCancellationResultDTO result = new PaymentCancellationResultDTO();
        String paymentType = "SEPA";

        // Act
        CancellationUtils.setCancellationNotSupported(result, paymentType);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(PaymentStatus.REJECTED, result.getStatus());
        assertEquals("CANCELLATION_NOT_SUPPORTED", result.getErrorCode());
        assertTrue(result.getErrorMessage().contains(paymentType));
    }
}
