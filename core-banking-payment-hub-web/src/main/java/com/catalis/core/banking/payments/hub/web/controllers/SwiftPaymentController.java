package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.SwiftPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.swift.SwiftScheduleRequestDTO;
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
 * REST controller for SWIFT payment operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling SWIFT payments.
 */
@RestController
@RequestMapping("/api/v1/payments/swift")
@Tag(name = "SWIFT Payments", description = "Operations for SWIFT payments (MT103, MT202, PACS.008)")
public class SwiftPaymentController {

    private static final Logger log = LoggerFactory.getLogger(SwiftPaymentController.class);

    private final SwiftPaymentService swiftPaymentService;

    @Autowired
    public SwiftPaymentController(SwiftPaymentService swiftPaymentService) {
        this.swiftPaymentService = swiftPaymentService;
        log.info("Initialized SwiftPaymentController");
    }

    @PostMapping(value = "/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate a SWIFT payment", description = "Simulates a SWIFT payment without actual execution. May require Strong Customer Authentication (SCA) for high-value payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulatePayment(
            @Parameter(description = "SWIFT payment request", required = true)
            @Valid @RequestBody SwiftPaymentRequestDTO request) {
        log.debug("Received SWIFT payment simulation request: {}", request);
        return swiftPaymentService.simulatePayment(request);
    }

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute a SWIFT payment", description = "Executes a SWIFT payment. Requires Strong Customer Authentication (SCA) for high-value payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution successful or pending SCA",
                    content = @Content(schema = @Schema(implementation = PaymentExecutionResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentExecutionResultDTO> executePayment(
            @Parameter(description = "SWIFT payment request", required = true)
            @Valid @RequestBody SwiftPaymentRequestDTO request) {
        log.debug("Received SWIFT payment execution request: {}", request);
        return swiftPaymentService.executePayment(request);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel a SWIFT payment", description = "Cancels an existing SWIFT payment. May require Strong Customer Authentication (SCA) for high-value payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation successful or pending SCA",
                    content = @Content(schema = @Schema(implementation = PaymentCancellationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentCancellationResultDTO> cancelPayment(
            @Parameter(description = "SWIFT cancellation request", required = true)
            @Valid @RequestBody SwiftCancellationRequestDTO request) {
        log.debug("Received SWIFT payment cancellation request: {}", request);
        return swiftPaymentService.cancelPayment(request);
    }

    @PostMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Schedule a SWIFT payment", description = "Schedules a SWIFT payment for future execution. May require Strong Customer Authentication (SCA) for high-value payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduling successful or pending SCA",
                    content = @Content(schema = @Schema(implementation = PaymentScheduleResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentScheduleResultDTO> schedulePayment(
            @Parameter(description = "SWIFT schedule request", required = true)
            @Valid @RequestBody SwiftScheduleRequestDTO request) {
        log.debug("Received SWIFT payment schedule request: {}", request);
        return swiftPaymentService.schedulePayment(request);
    }
}