package com.firefly.core.banking.payments.hub.web.controllers;

import com.firefly.core.banking.payments.hub.core.services.AchPaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.ach.AchCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.ach.AchTransferRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
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
 * REST controller for ACH (Automated Clearing House) payment operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling ACH payments.
 *
 * ACH is the primary electronic funds transfer system used in the United States for
 * bank-to-bank transfers. It processes large volumes of credit and debit transactions
 * in batches, including direct deposits, bill payments, and business-to-business payments.
 *
 * Key features of ACH payments:
 * - Typically settles in 1-3 business days
 * - Lower cost compared to wire transfers
 * - Supports both credit (push) and debit (pull) transactions
 * - Has specific processing windows and cut-off times
 * - Supports recurring payment schedules
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
    @Operation(summary = "Simulate an ACH payment",
               description = """
               **Simulates an ACH payment without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the payment request data and routing numbers
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees and exchange rates
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns estimated settlement date and processing information

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
            @Parameter(description = "ACH payment request", required = true)
            @Valid @RequestBody AchTransferRequestDTO request) {
        log.debug("Received ACH payment simulation request: {}", request);
        return achPaymentService.simulatePayment(request);
    }

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute an ACH payment",
               description = """
               **Executes an ACH payment by submitting it to the ACH network.**

               This endpoint processes the actual payment transaction:

               - Validates the payment request data and routing numbers
               - Verifies sufficient funds are available in the source account
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Submits the payment to the ACH network
               - Returns a transaction reference and expected settlement date

               **Settlement Time:** ACH payments typically settle in 1-3 business days depending on processing windows.

               **Required Fields:**
               - `simulationReference`: Reference from a previous successful simulation call
               - `sca.authenticationCode`: If SCA was required during simulation
               """)
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



    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel an ACH payment",
               description = """
               **Cancels an existing ACH payment with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted payment:

               - Validates the cancellation request and authorization
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the ACH network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - ACH payments can only be cancelled if they haven't been settled yet
               - Once a payment has been processed by the ACH network, cancellation may not be possible
               - A simulation reference from the `/cancel/simulate` endpoint must be included for SCA verification
               """)
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
               description = """
               **Simulates cancellation of an ACH payment without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request and authorization
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important:** The simulation reference returned must be included in the actual cancellation
               request to link the SCA verification. This endpoint does not actually cancel the payment.
               """)
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
    @Operation(summary = "Schedule an ACH payment",
               description = """
               **Schedules an ACH payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data and routing numbers
               - Verifies the execution date is valid (must be a future business day)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically executed on the specified date
               - Funds are not reserved until the actual execution date
               - Recurrence patterns use standard CRON expressions (e.g., `0 0 1 * *` for monthly on the 1st)
               - ACH payments are not processed on weekends or federal holidays
               """)
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
