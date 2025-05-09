package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.SepaPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for SEPA payment operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling SEPA payments.
 */
@RestController
@RequestMapping("/api/v1/payments/sepa")
@Tag(name = "SEPA Payments", description = "Operations for SEPA payments (SCT, ICT, SDD)")
public class SepaPaymentController {

    private static final Logger log = LoggerFactory.getLogger(SepaPaymentController.class);

    private final SepaPaymentService sepaPaymentService;

    @Autowired
    public SepaPaymentController(SepaPaymentService sepaPaymentService) {
        this.sepaPaymentService = sepaPaymentService;
        log.info("Initialized SepaPaymentController");
    }

    @PostMapping(value = "/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate a SEPA payment", description = "Simulates a SEPA payment without actual execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulatePayment(
            @Parameter(description = "SEPA payment request", required = true)
            @Valid @RequestBody SepaPaymentRequestDTO request) {
        log.debug("Received SEPA payment simulation request: {}", request);
        return sepaPaymentService.simulatePayment(request);
    }

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute a SEPA payment", description = "Executes a SEPA payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution successful",
                    content = @Content(schema = @Schema(implementation = PaymentExecutionResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentExecutionResultDTO> executePayment(
            @Parameter(description = "SEPA payment request", required = true)
            @Valid @RequestBody SepaPaymentRequestDTO request) {
        log.debug("Received SEPA payment execution request: {}", request);
        return sepaPaymentService.executePayment(request);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel a SEPA payment", description = "Cancels an existing SEPA payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation successful",
                    content = @Content(schema = @Schema(implementation = PaymentCancellationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentCancellationResultDTO> cancelPayment(
            @Parameter(description = "SEPA cancellation request", required = true)
            @Valid @RequestBody SepaCancellationRequestDTO request) {
        log.debug("Received SEPA payment cancellation request: {}", request);
        return sepaPaymentService.cancelPayment(request);
    }

    @PostMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Schedule a SEPA payment", description = "Schedules a SEPA payment for future execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduling successful",
                    content = @Content(schema = @Schema(implementation = PaymentScheduleResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentScheduleResultDTO> schedulePayment(
            @Parameter(description = "SEPA schedule request", required = true)
            @Valid @RequestBody SepaScheduleRequestDTO request) {
        log.debug("Received SEPA payment schedule request: {}", request);
        return sepaPaymentService.schedulePayment(request);
    }
}