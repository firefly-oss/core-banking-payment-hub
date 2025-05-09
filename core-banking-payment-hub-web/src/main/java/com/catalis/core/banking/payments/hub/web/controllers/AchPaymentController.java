package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.AchPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.ach.AchCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
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
 * REST controller for ACH payment operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling ACH payments.
 */
@RestController
@RequestMapping("/api/v1/payments/ach")
@Tag(name = "ACH Payments", description = "Operations for ACH payments (US bank transfers)")
public class AchPaymentController {

    private static final Logger log = LoggerFactory.getLogger(AchPaymentController.class);

    private final AchPaymentService achPaymentService;

    @Autowired
    public AchPaymentController(AchPaymentService achPaymentService) {
        this.achPaymentService = achPaymentService;
        log.info("Initialized AchPaymentController");
    }

    @PostMapping(value = "/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate an ACH payment", description = "Simulates an ACH payment without actual execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulatePayment(
            @Parameter(description = "ACH payment request", required = true)
            @Valid @RequestBody AchTransferRequestDTO request) {
        log.debug("Received ACH payment simulation request: {}", request);
        return achPaymentService.simulatePayment(request);
    }

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute an ACH payment", description = "Executes an ACH payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution successful",
                    content = @Content(schema = @Schema(implementation = PaymentExecutionResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentExecutionResultDTO> executePayment(
            @Parameter(description = "ACH payment request", required = true)
            @Valid @RequestBody AchTransferRequestDTO request) {
        log.debug("Received ACH payment execution request: {}", request);
        return achPaymentService.executePayment(request);
    }

    @PostMapping(value = "/cancel/{paymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel an ACH payment", description = "Cancels an existing ACH payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation successful",
                    content = @Content(schema = @Schema(implementation = PaymentCancellationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Deprecated
    public Mono<PaymentCancellationResultDTO> cancelPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true)
            @RequestParam("reason") @NotBlank String reason) {
        log.debug("Received ACH payment cancellation request for ID: {}, reason: {}", paymentId, reason);
        return achPaymentService.cancelPayment(paymentId, reason);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel an ACH payment", description = "Cancels an existing ACH payment with SCA support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation successful",
                    content = @Content(schema = @Schema(implementation = PaymentCancellationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentCancellationResultDTO> cancelPayment(
            @Parameter(description = "ACH cancellation request", required = true)
            @Valid @RequestBody AchCancellationRequestDTO request) {
        log.debug("Received ACH payment cancellation request: {}", request);
        return achPaymentService.cancelPayment(request);
    }

    @PostMapping(value = "/cancel/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate cancellation of an ACH payment",
               description = "Simulates cancellation of an ACH payment to trigger SCA delivery and provide information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulateCancellation(
            @Parameter(description = "ACH cancellation request", required = true)
            @Valid @RequestBody AchCancellationRequestDTO request) {
        log.debug("Received ACH payment cancellation simulation request: {}", request);
        return achPaymentService.simulateCancellation(request);
    }

    @PostMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Schedule an ACH payment", description = "Schedules an ACH payment for future execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduling successful",
                    content = @Content(schema = @Schema(implementation = PaymentScheduleResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentScheduleResultDTO> schedulePayment(
            @Parameter(description = "ACH payment request", required = true)
            @Valid @RequestBody AchTransferRequestDTO request,
            @Parameter(description = "Execution date (ISO format: YYYY-MM-DD)", required = true)
            @RequestParam("executionDate") @NotBlank String executionDate,
            @Parameter(description = "Recurrence pattern (CRON expression)")
            @RequestParam(value = "recurrencePattern", required = false) String recurrencePattern) {
        log.debug("Received ACH payment schedule request: {}, execution date: {}, recurrence: {}",
                request, executionDate, recurrencePattern);
        return achPaymentService.schedulePayment(request, executionDate, recurrencePattern);
    }
}
