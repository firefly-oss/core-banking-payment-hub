package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.InternalTransferService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for internal transfer operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling internal transfers.
 */
@RestController
@RequestMapping("/api/v1/payments/internal")
@Tag(name = "Internal Transfers", description = "Operations for internal transfers between accounts")
public class InternalTransferController {
    
    private static final Logger log = LoggerFactory.getLogger(InternalTransferController.class);
    
    private final InternalTransferService internalTransferService;
    
    @Autowired
    public InternalTransferController(InternalTransferService internalTransferService) {
        this.internalTransferService = internalTransferService;
        log.info("Initialized InternalTransferController");
    }
    
    @PostMapping(value = "/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate an internal transfer", description = "Simulates an internal transfer without actual execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulateTransfer(
            @Parameter(description = "Internal transfer request", required = true)
            @Valid @RequestBody InternalTransferRequestDTO request) {
        log.debug("Received internal transfer simulation request: {}", request);
        return internalTransferService.simulateTransfer(request);
    }
    
    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute an internal transfer", description = "Executes an internal transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution successful",
                    content = @Content(schema = @Schema(implementation = PaymentExecutionResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentExecutionResultDTO> executeTransfer(
            @Parameter(description = "Internal transfer request", required = true)
            @Valid @RequestBody InternalTransferRequestDTO request) {
        log.debug("Received internal transfer execution request: {}", request);
        return internalTransferService.executeTransfer(request);
    }
    
    @PostMapping(value = "/cancel/{transferId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel an internal transfer", description = "Cancels an existing internal transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation successful",
                    content = @Content(schema = @Schema(implementation = PaymentCancellationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Transfer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentCancellationResultDTO> cancelTransfer(
            @Parameter(description = "Transfer ID", required = true)
            @PathVariable("transferId") String transferId,
            @Parameter(description = "Cancellation reason", required = true)
            @RequestParam("reason") @NotBlank String reason) {
        log.debug("Received internal transfer cancellation request for ID: {}, reason: {}", transferId, reason);
        return internalTransferService.cancelTransfer(transferId, reason);
    }
    
    @PostMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Schedule an internal transfer", description = "Schedules an internal transfer for future execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduling successful",
                    content = @Content(schema = @Schema(implementation = PaymentScheduleResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentScheduleResultDTO> scheduleTransfer(
            @Parameter(description = "Internal transfer request", required = true)
            @Valid @RequestBody InternalTransferRequestDTO request,
            @Parameter(description = "Execution date (ISO format: YYYY-MM-DD)", required = true)
            @RequestParam("executionDate") @NotBlank String executionDate,
            @Parameter(description = "Recurrence pattern (CRON expression)")
            @RequestParam(value = "recurrencePattern", required = false) String recurrencePattern) {
        log.debug("Received internal transfer schedule request: {}, execution date: {}, recurrence: {}", 
                request, executionDate, recurrencePattern);
        return internalTransferService.scheduleTransfer(request, executionDate, recurrencePattern);
    }
}
