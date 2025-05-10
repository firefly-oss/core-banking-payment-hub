package com.catalis.core.banking.payments.hub.core.services.impl;

import com.catalis.core.banking.payments.hub.core.utils.ScaUtils;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.ScaResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.providers.ScaProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of the ScaProvider interface.
 * Provides functionality for triggering and validating SCA.
 */
@Component
public class DefaultScaProvider implements ScaProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultScaProvider.class);

    private static final BigDecimal SCA_THRESHOLD_AMOUNT = new BigDecimal("500.00");

    @Override
    public Mono<ScaResultDTO> triggerSca(String recipientIdentifier, String method, String referenceId) {
        log.info("Triggering SCA for recipient: {}, method: {}, reference: {}",
                ScaUtils.maskPhoneNumber(recipientIdentifier), method, referenceId);

        // In a real implementation, this would call an external service to deliver the SCA challenge
        // For simulation, we'll create a mock SCA result

        LocalDateTime expiryTimestamp = LocalDateTime.now().plusMinutes(15);
        ScaResultDTO scaResult = ScaUtils.createDefaultScaResult(method, expiryTimestamp);

        // Set additional information
        scaResult.setMessage("SCA challenge sent to " + ScaUtils.maskPhoneNumber(recipientIdentifier));

        // Log the simulated code for testing purposes
        log.debug("Simulated SCA code for challenge {}: 123456", scaResult.getChallengeId());

        return Mono.just(scaResult);
    }

    @Override
    public Mono<ScaResultDTO> validateSca(ScaDTO sca) {
        log.info("Validating SCA challenge: {}", sca.getChallengeId());

        // Delegate to the utility method for validation
        ScaResultDTO result = ScaUtils.validateSca(sca);

        log.info("SCA validation result for challenge {}: {}",
                sca.getChallengeId(), result.isSuccess() ? "SUCCESS" : "FAILED");

        return Mono.just(result);
    }

    @Override
    public Mono<Boolean> isScaRequired(String operationType, String amount, String currency, String accountId) {
        log.debug("Checking if SCA is required for operation: {}, amount: {}, currency: {}, account: {}",
                operationType, amount, currency, accountId);

        boolean scaRequired = false;

        // Check if amount exceeds threshold
        try {
            BigDecimal operationAmount = new BigDecimal(amount);
            if (operationAmount.compareTo(SCA_THRESHOLD_AMOUNT) > 0) {
                scaRequired = true;
                log.debug("SCA required due to amount exceeding threshold");
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid amount format: {}", amount);
        }

        // Additional risk-based checks could be implemented here
        // For example, checking if the operation is unusual for the account

        log.info("SCA requirement decision for operation {}: {}", operationType, scaRequired ? "REQUIRED" : "NOT REQUIRED");

        return Mono.just(scaRequired);
    }
}
