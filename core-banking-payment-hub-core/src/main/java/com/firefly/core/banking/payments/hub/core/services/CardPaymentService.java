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