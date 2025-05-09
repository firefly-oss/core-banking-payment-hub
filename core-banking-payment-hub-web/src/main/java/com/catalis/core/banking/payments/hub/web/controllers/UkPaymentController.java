package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.UkPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkBacsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkChapsPaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.uk.UkFasterPaymentRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for UK payment operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling UK payments
 * through the three main UK payment schemes: Faster Payments Service (FPS), BACS, and CHAPS.
 *
 * UK Payment Schemes:
 *
 * 1. Faster Payments Service (FPS):
 *    - Near real-time payments (typically within seconds or minutes)
 *    - 24/7/365 availability
 *    - Maximum transaction limit of £1,000,000
 *    - Used for urgent, same-day transfers
 *
 * 2. BACS (Bankers' Automated Clearing Services):
 *    - 3-day processing cycle (submit on day 1, process on day 2, complete on day 3)
 *    - Used for regular payments like salaries, direct debits, and supplier payments
 *    - Lower cost than FPS or CHAPS
 *    - Batch processing with specific submission windows
 *
 * 3. CHAPS (Clearing House Automated Payment System):
 *    - Same-day settlement for high-value transactions
 *    - Typically used for property purchases, tax payments, and large corporate transactions
 *    - No upper limit on transaction value
 *    - Only available during banking hours with specific cut-off times
 */
@RestController
@RequestMapping("/api/v1/payments/uk")
@Tag(name = "UK Payments", description = "API for UK payment operations (FPS, BACS, CHAPS)")
public class UkPaymentController {

    private final UkPaymentService ukPaymentService;

    @Autowired
    public UkPaymentController(UkPaymentService ukPaymentService) {
        this.ukPaymentService = ukPaymentService;
    }

    // UK Faster Payments endpoints

    @PostMapping("/fps/simulate")
    @Operation(summary = "Simulate a UK Faster Payment",
               description = """
               **Simulates a UK Faster Payment without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the payment request data including sort code and account number
               - Performs sort code validation against the UK payments directory
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns estimated processing time (typically seconds or minutes for FPS)

               **Important:** Use this endpoint before executing a payment to verify it will succeed
               and to obtain a simulation reference that must be included in the execute request.

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateFasterPayment(
            @Valid @RequestBody UkFasterPaymentRequestDTO request) {
        return ukPaymentService.simulateFasterPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/execute")
    @Operation(summary = "Execute a UK Faster Payment",
               description = """
               **Executes a UK Faster Payment through the Faster Payments Service.**

               This endpoint processes the actual payment transaction:

               - Validates the payment request data including sort code and account number
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Submits the payment to the Faster Payments network
               - Returns a transaction reference and confirmation details

               **Settlement Time:** Faster Payments typically settle within seconds or minutes and are
               available 24/7/365.

               **Transaction Limits:** Maximum transaction limit is typically £1,000,000, but may vary by bank.

               **Required Fields:**
               - `simulationReference`: Reference from a previous successful simulation call
               - `sca.authenticationCode`: If SCA was required during simulation

               """)
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeFasterPayment(
            @Valid @RequestBody UkFasterPaymentRequestDTO request) {
        return ukPaymentService.executeFasterPayment(request)
                .map(ResponseEntity::ok);
    }



    @PostMapping("/fps/cancel")
    @Operation(summary = "Cancel a UK Faster Payment",
               description = """
               **Cancels an existing UK Faster Payment with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted payment:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the Faster Payments network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - Faster Payments can only be cancelled if they haven't been settled yet
               - Due to the near real-time nature of the service, the cancellation window may be very short
               - Once settled, a return payment must be initiated instead
               - A simulation reference from the `/fps/cancel/simulate` endpoint must be included for SCA verification

               """)
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelFasterPayment(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.cancelFasterPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a UK Faster Payment",
               description = """
               **Simulates cancellation of a UK Faster Payment without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned must be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - The cancellation window for Faster Payments is very short due to near real-time settlement

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateFasterPaymentCancellation(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.simulateFasterPaymentCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/schedule")
    @Operation(summary = "Schedule a UK Faster Payment",
               description = """
               **Schedules a UK Faster Payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including sort code and account number
               - Verifies the execution date is valid (must be a future date)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically executed via the Faster Payments Service on the specified date
               - Funds are not reserved until the actual execution date
               - Recurrence patterns use standard CRON expressions to define repeating schedules
               - Since Faster Payments operate 24/7/365, payments can be scheduled for any date

               """)
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleFasterPayment(
            @Valid @RequestBody UkFasterPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ukPaymentService.scheduleFasterPayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // UK BACS endpoints

    @PostMapping("/bacs/simulate")
    @Operation(summary = "Simulate a UK BACS Payment",
               description = """
               **Simulates a UK BACS Payment without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the payment request data including sort code and account number
               - Performs sort code validation against the BACS directory
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns estimated settlement date based on BACS processing cycle

               **About BACS:**
               - BACS payments follow a 3-day processing cycle
               - Typically used for regular payments like salaries, direct debits, and supplier payments
               - Lower cost than Faster Payments or CHAPS

               **Important:** Use this endpoint before executing a payment to verify it will succeed
               and to obtain a simulation reference that must be included in the execute request.

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateBacsPayment(
            @Valid @RequestBody UkBacsPaymentRequestDTO request) {
        return ukPaymentService.simulateBacsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/execute")
    @Operation(summary = "Execute a UK BACS Payment",
               description = """
               **Executes a UK BACS Payment through the BACS network.**

               This endpoint processes the actual payment transaction:

               - Validates the payment request data including sort code and account number
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Submits the payment to the BACS network
               - Returns a transaction reference and expected settlement date

               **Settlement Time:** BACS payments follow a 3-day processing cycle:
               - Submit on day 1
               - Process on day 2
               - Complete on day 3

               **Common Uses:**
               - Salary and wage payments
               - Direct Debits for regular bills
               - Supplier payments
               - Regular business-to-business transfers

               **Required Fields:**
               - `simulationReference`: Reference from a previous successful simulation call
               - `sca.authenticationCode`: If SCA was required during simulation

               """)
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeBacsPayment(
            @Valid @RequestBody UkBacsPaymentRequestDTO request) {
        return ukPaymentService.executeBacsPayment(request)
                .map(ResponseEntity::ok);
    }



    @PostMapping("/bacs/cancel")
    @Operation(summary = "Cancel a UK BACS Payment",
               description = """
               **Cancels an existing UK BACS Payment with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted payment:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the BACS network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - BACS payments can typically be cancelled up until the end of the submission day (day 1 of the 3-day cycle)
               - Once the payment has moved to processing (day 2), cancellation is no longer possible
               - A simulation reference from the `/bacs/cancel/simulate` endpoint must be included for SCA verification

               """)
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelBacsPayment(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.cancelBacsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a UK BACS Payment",
               description = """
               **Simulates cancellation of a UK BACS Payment without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned must be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - BACS payments can typically be cancelled up until the end of the submission day (day 1 of the 3-day cycle)
               - Cancellation window depends on the BACS processing schedule and cut-off times

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateBacsPaymentCancellation(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.simulateBacsPaymentCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/schedule")
    @Operation(summary = "Schedule a UK BACS Payment",
               description = """
               **Schedules a UK BACS Payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including sort code and account number
               - Verifies the execution date is valid (must be a future business day)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the BACS network on the specified date
               - Funds are not reserved until the actual execution date
               - The actual settlement will occur 2 business days after the execution date due to the BACS processing cycle
               - BACS only processes payments on business days
               - Payments scheduled for weekends or holidays will be processed on the next business day

               """)
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleBacsPayment(
            @Valid @RequestBody UkBacsPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ukPaymentService.scheduleBacsPayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // UK CHAPS endpoints

    @PostMapping("/chaps/simulate")
    @Operation(summary = "Simulate a UK CHAPS Payment",
               description = """
               **Simulates a UK CHAPS Payment without actual execution.**

               This endpoint performs validation and preliminary checks without moving any funds:

               - Validates the payment request data including sort code and account number
               - Performs sort code validation against the CHAPS directory
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees (CHAPS typically has higher fees)
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)
               - Verifies the payment is within CHAPS operating hours
               - Returns estimated settlement time (same day if before cut-off time)

               **About CHAPS:**
               - Used for high-value or time-critical payments that need same-day settlement
               - No upper limit on transaction value
               - Suitable for property purchases, large corporate transactions, and time-critical payments
               - Only available during banking hours with specific cut-off times

               **Important:** Use this endpoint before executing a payment to verify it will succeed
               and to obtain a simulation reference that must be included in the execute request.

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateChapsPayment(
            @Valid @RequestBody UkChapsPaymentRequestDTO request) {
        return ukPaymentService.simulateChapsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/execute")
    @Operation(summary = "Execute a UK CHAPS Payment",
               description = """
               **Executes a UK CHAPS Payment through the CHAPS network.**

               This endpoint processes the actual payment transaction:

               - Validates the payment request data including sort code and account number
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Verifies the payment is within CHAPS operating hours
               - Submits the payment to the CHAPS network
               - Returns a transaction reference and expected settlement time

               **Settlement Time:** CHAPS payments settle on the same day if submitted before the cut-off time 
               (typically around 5:00 PM UK time).

               **Transaction Limits:** No upper limit on transaction value, making CHAPS suitable for:
               - Property purchases
               - Large corporate transactions
               - Time-critical high-value payments
               - Treasury operations

               **Required Fields:**
               - `simulationReference`: Reference from a previous successful simulation call
               - `sca.authenticationCode`: If SCA was required during simulation

               """)
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeChapsPayment(
            @Valid @RequestBody UkChapsPaymentRequestDTO request) {
        return ukPaymentService.executeChapsPayment(request)
                .map(ResponseEntity::ok);
    }



    @PostMapping("/chaps/cancel")
    @Operation(summary = "Cancel a UK CHAPS Payment",
               description = """
               **Cancels an existing UK CHAPS Payment with Strong Customer Authentication (SCA) support.**

               This endpoint attempts to cancel a previously submitted payment:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the CHAPS network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - CHAPS payments are typically difficult to cancel once submitted due to their high-priority nature
               - Cancellation is generally only possible if the payment has not yet been settled
               - The cancellation window is very short due to same-day settlement
               - A simulation reference from the `/chaps/cancel/simulate` endpoint must be included for SCA verification

               """)
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelChapsPayment(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.cancelChapsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a UK CHAPS Payment",
               description = """
               **Simulates cancellation of a UK CHAPS Payment without actually cancelling it.**

               This endpoint is used to trigger SCA delivery and provide information about cancellation:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned must be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - CHAPS payments are typically difficult to cancel once submitted due to their high-priority nature
               - The cancellation window is very short due to same-day settlement

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateChapsPaymentCancellation(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.simulateChapsPaymentCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/schedule")
    @Operation(summary = "Schedule a UK CHAPS Payment", 
               description = """
               **Schedules a UK CHAPS Payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including sort code and account number
               - Verifies the execution date is valid (must be a future business day)
               - Verifies SCA if required (using the code delivered during simulation)
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the CHAPS network on the specified date
               - Funds are not reserved until the actual execution date
               - CHAPS only operates on business days during banking hours
               - Payments scheduled for weekends, holidays, or after cut-off times will be processed on the next business day
               - For time-critical payments, consider the cut-off time (typically around 5:00 PM UK time)

               """)
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleChapsPayment(
            @Valid @RequestBody UkChapsPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ukPaymentService.scheduleChapsPayment(request, executionDate)
                .map(ResponseEntity::ok);
    }
}
