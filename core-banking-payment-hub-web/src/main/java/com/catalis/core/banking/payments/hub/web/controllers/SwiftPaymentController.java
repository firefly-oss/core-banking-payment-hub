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
 *
 * SWIFT (Society for Worldwide Interbank Financial Telecommunication) is a global network that enables
 * financial institutions to send and receive information about financial transactions in a secure,
 * standardized environment. SWIFT payments are characterized by:
 *
 * - Global reach across more than 200 countries and territories
 * - Support for multiple currencies and high-value transactions
 * - Standardized message formats (MT and MX/ISO 20022 series)
 * - Correspondent banking relationships for cross-border transfers
 * - Typically settles within 1-3 business days depending on currency and destination
 * - Higher fees compared to domestic payment methods
 * - Enhanced security and compliance requirements
 *
 * Common SWIFT message types:
 * - MT103: Single Customer Credit Transfer
 * - MT202: General Financial Institution Transfer
 * - PACS.008: FI-to-FI Customer Credit Transfer (ISO 20022)
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
    @Operation(summary = "Simulate a SWIFT payment",
               description = """
               **Simulates a SWIFT payment without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the payment request data including BIC codes and account details
               - Verifies the receiving bank is reachable through the SWIFT network
               - Checks for sufficient funds (without placing a hold)
               - Calculates applicable fees (which can be significant for SWIFT transfers)
               - Determines if Strong Customer Authentication (SCA) is required based on amount and destination
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns estimated settlement date and routing information

               **Important:** Use this endpoint before executing a payment to verify it will succeed
               and to obtain a simulation reference that must be included in the execute request.

               """)
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
    @Operation(summary = "Execute a SWIFT payment",
               description = """
               **Executes a SWIFT payment through the SWIFT network.**

               This endpoint processes the actual payment transaction:

               - Validates the payment request data including BIC codes and account details
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Submits the payment to the SWIFT network
               - Returns a transaction reference and expected settlement date

               **Settlement Time:** SWIFT payments typically settle within 1-3 business days depending
               on the destination country and currency.

               **Important Notes:**
               - Correspondent banks may be involved in the payment chain
               - This can affect settlement time and may incur additional fees
               - Include the simulation reference if SCA was required

               """)
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

    @PostMapping(value = "/cancel/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate cancellation of a SWIFT payment",
               description = """
               **Simulates cancellation of a SWIFT payment without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned must be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - SWIFT payments can be difficult to cancel once they've entered the correspondent banking network

               """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulateCancellation(
            @Parameter(description = "SWIFT cancellation request", required = true)
            @Valid @RequestBody SwiftCancellationRequestDTO request) {
        log.debug("Received SWIFT payment cancellation simulation request: {}", request);
        return swiftPaymentService.simulateCancellation(request);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel a SWIFT payment",
               description = """
               **Cancels an existing SWIFT payment with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted payment:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the SWIFT network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - SWIFT payments can only be cancelled if they haven't been settled yet
               - Once a payment has been processed by correspondent banks, cancellation may not be possible
               - A separate return payment might be required if cancellation fails
               - A simulation reference from the `/cancel/simulate` endpoint must be included for SCA verification

               """)
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
    @Operation(summary = "Schedule a SWIFT payment",
               description = """
               **Schedules a SWIFT payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including BIC codes and account details
               - Verifies the execution date is valid (must be a future business day)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the SWIFT network on the specified date
               - Funds are not reserved until the actual execution date
               - SWIFT payments are typically processed only on business days
               - Payments scheduled for weekends or holidays will be processed on the next business day

               """)
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
