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


package com.firefly.core.banking.payments.hub.interfaces.dtos.internal;

import com.firefly.core.banking.payments.hub.interfaces.dtos.common.AccountInfoDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.BasePaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PartyInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request for internal transfers between accounts within the core banking system.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Request for internal transfers between accounts within the core banking system")
public class InternalTransferRequestDTO extends BasePaymentRequestDTO {

    @Valid
    @NotNull(message = "Source account information is required")
    @Schema(description = "Source account information", required = true)
    private AccountInfoDTO sourceAccount;

    @Valid
    @NotNull(message = "Destination account information is required")
    @Schema(description = "Destination account information", required = true)
    private AccountInfoDTO destinationAccount;

    @Valid
    @Schema(description = "Source account owner information")
    private PartyInfoDTO sourceAccountOwner;

    @Valid
    @Schema(description = "Destination account owner information")
    private PartyInfoDTO destinationAccountOwner;

    @NotBlank(message = "Transfer description is required")
    @Size(max = 140, message = "Transfer description must not exceed 140 characters")
    @Schema(description = "Description of the transfer", example = "Monthly rent payment", required = true)
    private String description;

    @Schema(description = "Category of the transfer", example = "RENT")
    private String category;

    @Schema(description = "Flag indicating if this is a recurring transfer")
    private Boolean recurring;

    @Schema(description = "Frequency of recurring transfer (e.g., DAILY, WEEKLY, MONTHLY)")
    private String recurringFrequency;

    @Schema(description = "Flag indicating if notification should be sent to the destination account owner")
    private Boolean notifyDestination;

    @Schema(description = "Flag indicating if this transfer is part of a batch operation")
    private Boolean batchTransfer;

    @Schema(description = "Batch ID if this transfer is part of a batch operation")
    private String batchId;
}
