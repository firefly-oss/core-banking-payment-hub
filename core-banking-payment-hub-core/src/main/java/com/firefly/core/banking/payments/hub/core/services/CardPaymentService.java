package com.firefly.core.banking.payments.hub.core.services;

import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.card.CardScheduleRequestDTO;

/**
 * Service interface for card payment operations.
 * Extends the generic PaymentService with card-specific request types.
 * 
 * This service acts as the "card authorization center" for all card payment operations,
 * following the same hexagonal architecture pattern used throughout the payment hub.
 */
public interface CardPaymentService extends PaymentService<CardPaymentRequestDTO, CardCancellationRequestDTO, CardScheduleRequestDTO> {
    // Additional card-specific methods can be added here
}