package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.InternalTransferService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.internal.InternalTransferCancellationRequestDTO;
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
 * Exposes endpoints for simulating, executing, cancelling, and scheduling internal transfers
 * between accounts within the same financial institution.
 *
 * Internal transfers are movements of funds between accounts owned by the same customer
 * or between different customers within the same banking system. These transfers:
 * - Are typically processed in real-time or near real-time
 * - Do not require external payment networks
 * - Usually have no or minimal fees
 * - May still require Strong Customer Authentication (SCA) for security
 * - Can be executed immediately or scheduled for future dates
 * - Support recurring payment patterns
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
    @Operation(summary = "Simulate an internal transfer",
               description = """
               **Simulates an internal transfer without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the transfer request data and account ownership
               - Checks for sufficient funds in the source account
               - Verifies account status (not frozen, closed, etc.)
               - Calculates any applicable fees (typically none for internal transfers)
               - Determines if Strong Customer Authentication (SCA) is required based on amount and risk
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns a simulation reference to be used in the execute endpoint

               **Important:** Use this endpoint before executing a transfer to verify it will succeed
               and to complete any required SCA process.

               """)
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
    @Operation(summary = "Execute an internal transfer",
               description = """
               **Executes an internal transfer between accounts.**

               This endpoint processes the actual transfer transaction:

               - Validates the transfer request data and account ownership
               - Verifies sufficient funds are available in the source account
               - Verifies SCA if required (using the code delivered during simulation)
               - Debits the source account and credits the destination account
               - Creates transaction records for both accounts
               - Returns a transaction reference and confirmation details

               **Settlement Time:** Internal transfers are typically processed immediately, with funds
               available to the recipient right away.

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
    public Mono<PaymentExecutionResultDTO> executeTransfer(
            @Parameter(description = "Internal transfer request", required = true)
            @Valid @RequestBody InternalTransferRequestDTO request) {
        log.debug("Received internal transfer execution request: {}", request);
        return internalTransferService.executeTransfer(request);
    }



    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel an internal transfer",
               description = """
               **Cancels an existing internal transfer with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted transfer:

               - Validates the cancellation request and verifies authorization
               - Checks if the transfer exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Reverses the transfer by crediting the source account and debiting the destination account
               - Creates reversal transaction records for both accounts
               - Returns the cancellation status and confirmation details

               **Important Limitations:**
               - Only pending or scheduled transfers can be cancelled
               - Completed transfers require a new transfer in the opposite direction
               - A simulation reference from the `/cancel/simulate` endpoint must be included for SCA verification

               """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation successful",
                    content = @Content(schema = @Schema(implementation = PaymentCancellationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Transfer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentCancellationResultDTO> cancelTransfer(
            @Valid @RequestBody InternalTransferCancellationRequestDTO request) {
        log.debug("Received internal transfer cancellation request: {}", request);
        return internalTransferService.cancelTransfer(request);
    }

    @PostMapping(value = "/cancel/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate cancellation of an internal transfer",
               description = """
               **Simulates cancellation of an internal transfer without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request and verifies authorization
               - Checks if the transfer exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility and SCA requirements
               - Provides a simulation reference to be used in the actual cancellation request

               **Important:** This endpoint does not actually cancel the transfer. Use the returned
               simulation reference in the subsequent call to the `/cancel` endpoint.

               """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Transfer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulateCancellation(
            @Valid @RequestBody InternalTransferCancellationRequestDTO request) {
        log.debug("Received internal transfer cancellation simulation request: {}", request);
        return internalTransferService.simulateCancellation(request);
    }

    @PostMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Schedule an internal transfer",
               description = """
               **Schedules an internal transfer for future execution on the specified date.**

               This endpoint creates a scheduled transfer instruction:

               - Validates the transfer request data and account ownership
               - Verifies the execution date is valid (must be a future date)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled transfer instruction
               - Supports optional recurrence patterns for recurring transfers
               - Returns a schedule reference and confirmation details

               **Important Notes:**
               - The transfer will be automatically executed on the specified date
               - Funds are not reserved until the actual execution date
               - Recurrence patterns use standard CRON expressions to define repeating schedules
               - Examples: `0 0 1 * *` (monthly on the 1st), `0 0 * * 1` (every Monday)

               """)
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
