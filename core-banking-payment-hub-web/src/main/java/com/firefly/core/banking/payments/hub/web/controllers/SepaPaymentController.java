package com.firefly.core.banking.payments.hub.web.controllers;

import com.firefly.core.banking.payments.hub.core.services.SepaPaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaPaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.sepa.SepaScheduleRequestDTO;
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
 *
 * SEPA (Single Euro Payments Area) is a payment integration initiative of the European Union for
 * simplifying bank transfers denominated in euro. SEPA enables customers to make cashless euro payments
 * to anyone located within the SEPA area using a single bank account and a single set of payment instruments.
 *
 * SEPA payment types include:
 *
 * 1. SEPA Credit Transfer (SCT):
 *    - Standard euro transfers within the SEPA zone
 *    - Typically settles within 1 business day
 *    - Maximum amount is typically unlimited
 *    - Used for regular business and personal transfers
 *
 * 2. SEPA Instant Credit Transfer (SCT Inst):
 *    - Real-time euro transfers available 24/7/365
 *    - Settles within 10 seconds
 *    - Maximum amount currently €100,000
 *    - Used for urgent transfers requiring immediate settlement
 *
 * 3. SEPA Direct Debit (SDD):
 *    - Pre-authorized pull payments from a debtor's account
 *    - Requires a mandate from the debtor
 *    - Available in Core (consumer) and B2B (business) schemes
 *    - Used for recurring payments and one-off collections
 *
 * SEPA covers all EU member states plus additional European countries and territories.
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
    @Operation(summary = "Simulate a SEPA payment",
               description = """
               **Simulates a SEPA payment without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the payment request data including IBAN and BIC
               - Verifies the receiving bank is reachable through SEPA
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees
               - Determines if Strong Customer Authentication (SCA) is required based on amount and risk
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns estimated settlement date based on the SEPA scheme (standard or instant)

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
            @Parameter(description = "SEPA payment request", required = true)
            @Valid @RequestBody SepaPaymentRequestDTO request) {
        log.debug("Received SEPA payment simulation request: {}", request);
        return sepaPaymentService.simulatePayment(request);
    }

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute a SEPA payment",
               description = """
               **Executes a SEPA payment through the SEPA network.**

               This endpoint processes the actual payment transaction:

               - Validates the payment request data including IBAN and BIC
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Submits the payment to the SEPA network
               - Returns a transaction reference and expected settlement date

               **Settlement Times:**
               - Standard SEPA Credit Transfers (SCT): typically settle within 1 business day
               - SEPA Instant Credit Transfers (SCT Inst): settle within 10 seconds
               - SEPA Direct Debits (SDD): follow specific settlement cycles based on the scheme

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
            @Parameter(description = "SEPA payment request", required = true)
            @Valid @RequestBody SepaPaymentRequestDTO request) {
        log.debug("Received SEPA payment execution request: {}", request);
        return sepaPaymentService.executePayment(request);
    }

    @PostMapping(value = "/cancel/simulate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate cancellation of a SEPA payment",
               description = """
               **Simulates cancellation of a SEPA payment without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned must be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - For SEPA Instant Credit Transfers, the cancellation window is very short due to real-time settlement
               - For SEPA Direct Debits, specific rules apply for cancellation before the collection date

               """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation successful",
                    content = @Content(schema = @Schema(implementation = PaymentSimulationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<PaymentSimulationResultDTO> simulateCancellation(
            @Parameter(description = "SEPA cancellation request", required = true)
            @Valid @RequestBody SepaCancellationRequestDTO request) {
        log.debug("Received SEPA payment cancellation simulation request: {}", request);
        return sepaPaymentService.simulateCancellation(request);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel a SEPA payment",
               description = """
               **Cancels an existing SEPA payment with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted payment:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the SEPA network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - SEPA payments can only be cancelled if they haven't been settled yet
               - For SEPA Instant Credit Transfers, the cancellation window is very short due to real-time settlement
               - For SEPA Direct Debits, specific rules apply for cancellation before the collection date
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
            @Parameter(description = "SEPA cancellation request", required = true)
            @Valid @RequestBody SepaCancellationRequestDTO request) {
        log.debug("Received SEPA payment cancellation request: {}", request);
        return sepaPaymentService.cancelPayment(request);
    }

    @PostMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Schedule a SEPA payment",
               description = """
               **Schedules a SEPA payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including IBAN and BIC
               - Verifies the execution date is valid (must be a future date for SCT, or a valid collection date for SDD)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the SEPA network on the specified date
               - Funds are not reserved until the actual execution date
               - For SEPA Direct Debits, specific rules apply regarding advance notification and mandate validation
               - Recurrence patterns use standard CRON expressions to define repeating schedules

               """)
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
