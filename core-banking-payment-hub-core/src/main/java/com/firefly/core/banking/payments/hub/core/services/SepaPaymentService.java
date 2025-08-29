package com.firefly.core.banking.payments.hub.core.services;

import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;

/**
 * Service interface for SEPA payment operations.
 * Extends the generic PaymentService with SEPA-specific request types.
 */
public interface SepaPaymentService extends PaymentService<SepaPaymentRequestDTO, SepaCancellationRequestDTO, SepaScheduleRequestDTO> {
    // Additional SEPA-specific methods can be added here
}