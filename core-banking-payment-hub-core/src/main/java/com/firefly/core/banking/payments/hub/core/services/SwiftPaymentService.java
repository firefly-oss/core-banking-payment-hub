package com.firefly.core.banking.payments.hub.core.services;

import com.firefly.core.banking.payments.hub.interfaces.dtos.swift.SwiftCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.swift.SwiftScheduleRequestDTO;

/**
 * Service interface for SWIFT payment operations.
 * Extends the generic PaymentService with SWIFT-specific request types.
 */
public interface SwiftPaymentService extends PaymentService<SwiftPaymentRequestDTO, SwiftCancellationRequestDTO, SwiftScheduleRequestDTO> {
    // Additional SWIFT-specific methods can be added here
}