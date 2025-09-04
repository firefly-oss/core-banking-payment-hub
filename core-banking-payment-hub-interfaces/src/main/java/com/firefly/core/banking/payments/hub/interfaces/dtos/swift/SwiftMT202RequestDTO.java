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


package com.firefly.core.banking.payments.hub.interfaces.dtos.swift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * SWIFT MT202 (Financial Institution Transfer) payment request.
 * Extends SwiftPaymentRequestDTO with MT202-specific fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SWIFT MT202 (Financial Institution Transfer) payment request")
public class SwiftMT202RequestDTO extends SwiftPaymentRequestDTO {
    
    @Schema(description = "Related reference", example = "REL-REF-12345")
    private String relatedReference;
    
    @Schema(description = "Time indication", example = "CLSTIME")
    private String timeIndication;
    
    @Schema(description = "Settlement priority", example = "URGP")
    private String settlementPriority;
    
    @Schema(description = "Settlement method", example = "COVE")
    private String settlementMethod;
    
    @Schema(description = "Clearing system", example = "CHIPS")
    private String clearingSystem;
    
    @Schema(description = "Sender to receiver information", example = "Please process with high priority")
    private String senderToReceiverInformation;
    
    @Schema(description = "Bank operation code", example = "CRED")
    private String bankOperationCode;
    
    @Schema(description = "Exchange rate", example = "1.1050")
    private String exchangeRate;
}
