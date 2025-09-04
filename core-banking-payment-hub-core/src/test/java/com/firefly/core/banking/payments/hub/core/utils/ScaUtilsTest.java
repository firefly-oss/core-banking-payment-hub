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

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ScaUtilsTest {

    @Test
    void validateSca_withValidCode_shouldReturnSuccess() {
        // Arrange
        ScaDTO sca = new ScaDTO();
        sca.setMethod("SMS");
        sca.setAuthenticationCode("123456"); // Valid code

        // Act
        ScaResultDTO result = ScaUtils.validateSca(sca);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("SMS", result.getMethod());
        assertNotNull(result.getChallengeId());
        assertNotNull(result.getVerificationTimestamp());
        assertEquals(1, result.getAttemptCount());
        assertEquals(3, result.getMaxAttempts());
        assertFalse(result.isExpired());
        assertNotNull(result.getExpiryTimestamp());
    }

    @Test
    void validateSca_withMissingCode_shouldReturnFailure() {
        // Arrange
        ScaDTO sca = new ScaDTO();
        sca.setMethod("SMS");
        sca.setAuthenticationCode(null);

        // Act
        ScaResultDTO result = ScaUtils.validateSca(sca);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("SCA_CODE_MISSING", result.getErrorCode());
        assertEquals("Authentication code is required", result.getErrorMessage());
    }

    @Test
    void maskPhoneNumber_withValidPhoneNumber_shouldMaskAllButLastFourDigits() {
        // Arrange
        String phoneNumber = "+1234567890";

        // Act
        String maskedNumber = ScaUtils.maskPhoneNumber(phoneNumber);

        // Assert
        assertEquals("*****7890", maskedNumber);
    }

    @Test
    void maskPhoneNumber_withShortPhoneNumber_shouldReturnAsIs() {
        // Arrange
        String phoneNumber = "1234";

        // Act
        String maskedNumber = ScaUtils.maskPhoneNumber(phoneNumber);

        // Assert
        assertEquals("1234", maskedNumber);
    }

    @Test
    void maskPhoneNumber_withNullPhoneNumber_shouldReturnNull() {
        // Act
        String maskedNumber = ScaUtils.maskPhoneNumber(null);

        // Assert
        assertNull(maskedNumber);
    }

    @Test
    void generateSimulationReference_shouldReturnValidReference() {
        // Act
        String reference = ScaUtils.generateSimulationReference();

        // Assert
        assertNotNull(reference);
        assertTrue(reference.startsWith("SIM-"));
        assertEquals(12, reference.length()); // "SIM-" + 8 characters
    }

    @Test
    void generateChallengeId_shouldReturnValidId() {
        // Act
        String challengeId = ScaUtils.generateChallengeId();

        // Assert
        assertNotNull(challengeId);
        assertTrue(challengeId.startsWith("CHL-"));
        assertEquals(12, challengeId.length()); // "CHL-" + 8 characters
    }

    @Test
    void createDefaultScaResult_shouldReturnValidResult() {
        // Arrange
        String method = "SMS";
        LocalDateTime expiryTimestamp = LocalDateTime.now().plusMinutes(15);

        // Act
        ScaResultDTO result = ScaUtils.createDefaultScaResult(method, expiryTimestamp);

        // Assert
        assertEquals(method, result.getMethod());
        assertTrue(result.getChallengeId().startsWith("CHL-"));
        assertNull(result.getVerificationTimestamp());
        assertEquals(0, result.getAttemptCount());
        assertEquals(3, result.getMaxAttempts());
        assertFalse(result.isExpired());
        assertEquals(expiryTimestamp, result.getExpiryTimestamp());
        assertFalse(result.isSuccess());
    }
}
