package com.firefly.core.banking.payments.hub.web.controllers;

import com.firefly.core.banking.payments.hub.core.services.EbaStep2PaymentService;
import com.firefly.core.banking.payments.hub.core.services.Target2PaymentService;
import com.firefly.core.banking.payments.hub.core.services.TipsPaymentService;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.EbaStep2CancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.EbaStep2PaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.Target2CancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.Target2PaymentRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.TipsCancellationRequestDTO;
import com.firefly.core.banking.payments.hub.interfaces.dtos.european.TipsPaymentRequestDTO;
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
 * REST controller for European payment operations.
 * Exposes endpoints for simulating, executing, cancelling, and scheduling European payments
 * through the three main European payment schemes: TARGET2, TIPS, and EBA STEP2.
 *
 * European Payment Schemes:
 *
 * 1. TARGET2 (Trans-European Automated Real-time Gross Settlement Express Transfer):
 *    - Real-time gross settlement system for euro payments
 *    - Used for high-value, time-critical payments between financial institutions
 *    - Settles in central bank money with immediate finality
 *    - Operates during business hours on TARGET2 business days
 *
 * 2. TIPS (TARGET Instant Payment Settlement):
 *    - Instant payment service available 24/7/365
 *    - Settles payments in central bank money in seconds
 *    - Maximum amount per transaction is currently €100,000
 *    - Designed for retail payments requiring immediate settlement
 *
 * 3. EBA STEP2 (European Banking Association STEP2):
 *    - Pan-European Automated Clearing House for bulk euro payments
 *    - Processes both SEPA Credit Transfers and SEPA Direct Debits
 *    - Operates on a batch processing model with multiple daily cycles
 *    - Typically settles within 1 business day
 */
@RestController
@RequestMapping("/api/v1/payments/european")
@Tag(name = "European Payments", description = "API for European payment operations (TARGET2, TIPS, EBA STEP2)")
public class EuropeanPaymentController {

    private final Target2PaymentService target2PaymentService;
    private final TipsPaymentService tipsPaymentService;
    private final EbaStep2PaymentService ebaStep2PaymentService;

    @Autowired
    public EuropeanPaymentController(
            Target2PaymentService target2PaymentService,
            TipsPaymentService tipsPaymentService,
            EbaStep2PaymentService ebaStep2PaymentService) {
        this.target2PaymentService = target2PaymentService;
        this.tipsPaymentService = tipsPaymentService;
        this.ebaStep2PaymentService = ebaStep2PaymentService;
    }

    // TARGET2 endpoints

    @PostMapping("/target2/simulate")
    @Operation(summary = "Simulate a TARGET2 payment",
               description = """
               **Simulates a TARGET2 payment without actual execution.**

               The simulation process:

               - Validates the payment request data including IBAN and BIC
               - Verifies the receiving bank is a TARGET2 participant
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)
               - Verifies the payment is within TARGET2 operating hours
               - Returns estimated settlement time (same day if before cut-off time)

               **Important Notes:**
               - TARGET2 is used for high-value, time-critical euro payments between financial institutions
               - Use this endpoint before executing a payment to verify it will succeed
               - The simulation reference returned must be included in the execute request if SCA is required

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTarget2Payment(
            @Valid @RequestBody Target2PaymentRequestDTO request) {
        return target2PaymentService.simulatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/execute")
    @Operation(summary = "Execute a TARGET2 payment",
               description = """
               **Executes a TARGET2 payment through the TARGET2 network.**

               This operation:

               - Validates the payment request data including IBAN and BIC
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Verifies the payment is within TARGET2 operating hours
               - Submits the payment to the TARGET2 network
               - Returns a transaction reference and expected settlement time

               **Settlement Information:**
               - TARGET2 payments settle in real-time with immediate finality
               - Operating hours are typically 7:00 AM to 6:00 PM CET on TARGET2 business days
               - Used for high-value, time-critical euro payments between financial institutions

               """)
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeTarget2Payment(
            @Valid @RequestBody Target2PaymentRequestDTO request) {
        return target2PaymentService.executePayment(request)
                .map(ResponseEntity::ok);
    }



    @PostMapping("/target2/cancel")
    @Operation(summary = "Cancel a TARGET2 payment",
               description = """
               **Cancels an existing TARGET2 payment with Strong Customer Authentication (SCA) support.**

               The process:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the TARGET2 network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - TARGET2 payments are typically difficult to cancel once submitted due to their real-time settlement nature
               - Cancellation is generally only possible if the payment has not yet been settled
               - Include the simulation reference from the /target2/cancel/simulate endpoint for SCA verification

               """)
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelTarget2Payment(
            @Valid @RequestBody Target2CancellationRequestDTO request) {
        return target2PaymentService.cancelPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a TARGET2 payment",
               description = """
               **Simulates cancellation of a TARGET2 payment to trigger SCA delivery and provide information.**

               The process:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned should be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - TARGET2 payments are typically difficult to cancel once submitted due to their real-time settlement nature

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTarget2PaymentCancellation(
            @Valid @RequestBody Target2CancellationRequestDTO request) {
        return target2PaymentService.simulateCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/schedule")
    @Operation(summary = "Schedule a TARGET2 payment",
               description = """
               **Schedules a TARGET2 payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including IBAN and BIC
               - Verifies the execution date is valid (must be a future TARGET2 business day)
               - Verifies SCA if required
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the TARGET2 network on the specified date
               - Funds are not reserved until the actual execution date
               - TARGET2 only operates on business days
               - Payments scheduled for weekends or holidays will be processed on the next business day

               """)
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleTarget2Payment(
            @Valid @RequestBody Target2PaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return target2PaymentService.schedulePayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // TIPS endpoints

    @PostMapping("/tips/simulate")
    @Operation(summary = "Simulate a TIPS payment",
               description = """
               **Simulates a TIPS payment without actual execution.**

               The simulation process:

               - Validates the payment request data including IBAN and BIC
               - Verifies the receiving bank is a TIPS participant
               - Checks for sufficient funds (without placing a hold)
               - Verifies the payment amount is within TIPS limits (currently €100,000)
               - Calculates any applicable fees
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)

               **About TIPS:**
               - TIPS (TARGET Instant Payment Settlement) is an instant payment service available 24/7/365
               - Settles payments in central bank money in seconds
               - Designed for retail payments requiring immediate settlement

               **Important:**
               - Use this endpoint before executing a payment to verify it will succeed
               - The simulation reference must be included in the execute request if SCA is required

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTipsPayment(
            @Valid @RequestBody TipsPaymentRequestDTO request) {
        return tipsPaymentService.simulatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/execute")
    @Operation(summary = "Execute a TIPS payment",
               description = """
               **Executes a TIPS payment through the TIPS network.**

               This operation:

               - Validates the payment request data including IBAN and BIC
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Verifies the payment amount is within TIPS limits (currently €100,000)
               - Submits the payment to the TIPS network
               - Returns a transaction reference and confirmation of instant settlement

               **Settlement Information:**
               - TIPS payments settle in real-time (typically within 10 seconds) with immediate finality
               - Available 24/7/365 including weekends and holidays
               - Used for retail payments requiring immediate settlement
               - Current maximum amount is €100,000 per transaction

               """)
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeTipsPayment(
            @Valid @RequestBody TipsPaymentRequestDTO request) {
        return tipsPaymentService.executePayment(request)
                .map(ResponseEntity::ok);
    }



    @PostMapping("/tips/cancel")
    @Operation(summary = "Cancel a TIPS payment",
               description = """
               **Cancels an existing TIPS payment with Strong Customer Authentication (SCA) support.**

               The process:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the TIPS network
               - Returns the cancellation status and any applicable fees

               **Important Limitations:**
               - TIPS payments are extremely difficult to cancel once submitted due to their instant settlement nature
               - Settlement typically occurs within 10 seconds
               - Cancellation is generally only possible if the payment is still in a queued state
               - Include the simulation reference from the /tips/cancel/simulate endpoint for SCA verification

               """)
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelTipsPayment(
            @Valid @RequestBody TipsCancellationRequestDTO request) {
        return tipsPaymentService.cancelPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a TIPS payment",
               description = """
               **Simulates cancellation of a TIPS payment to trigger SCA delivery and provide information.**

               The process:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned should be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - TIPS payments are extremely difficult to cancel once submitted due to their instant settlement nature
               - Settlement typically occurs within 10 seconds

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTipsPaymentCancellation(
            @Valid @RequestBody TipsCancellationRequestDTO request) {
        return tipsPaymentService.simulateCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/schedule")
    @Operation(summary = "Schedule a TIPS payment",
               description = """
               **Schedules a TIPS payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including IBAN and BIC
               - Verifies the execution date is valid (must be a future date)
               - Verifies the payment amount is within TIPS limits (currently €100,000)
               - Verifies SCA if required
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the TIPS network on the specified date
               - Funds are not reserved until the actual execution date
               - Since TIPS operates 24/7/365, payments can be scheduled for any date
               - This includes weekends and holidays

               """)
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleTipsPayment(
            @Valid @RequestBody TipsPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return tipsPaymentService.schedulePayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // EBA STEP2 endpoints

    @PostMapping("/step2/simulate")
    @Operation(summary = "Simulate an EBA STEP2 payment",
               description = """
               **Simulates an EBA STEP2 payment without actual execution.**

               The simulation process:

               - Validates the payment request data including IBAN and BIC
               - Verifies the receiving bank is reachable through EBA STEP2
               - Checks for sufficient funds (without placing a hold)
               - Calculates any applicable fees
               - Determines if Strong Customer Authentication (SCA) is required
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns estimated settlement date based on STEP2 processing cycles

               **About EBA STEP2:**
               - Pan-European Automated Clearing House for bulk euro payments
               - Processes both SEPA Credit Transfers and SEPA Direct Debits
               - Operates on a batch processing model with multiple daily cycles
               - Typically settles within 1 business day

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateEbaStep2Payment(
            @Valid @RequestBody EbaStep2PaymentRequestDTO request) {
        return ebaStep2PaymentService.simulatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/execute")
    @Operation(summary = "Execute an EBA STEP2 payment",
               description = """
               **Executes an EBA STEP2 payment through the EBA STEP2 network.**

               This operation:

               - Validates the payment request data including IBAN and BIC
               - Verifies sufficient funds are available
               - Places a hold on the funds
               - Verifies SCA if required (using the code delivered during simulation)
               - Submits the payment to the EBA STEP2 network
               - Returns a transaction reference and expected settlement date

               **Settlement Information:**
               - EBA STEP2 payments are processed in batches with multiple daily cycles
               - Payments are typically settled within 1 business day
               - STEP2 is used for SEPA Credit Transfers and SEPA Direct Debits
               - Covers the entire Single Euro Payments Area

               """)
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeEbaStep2Payment(
            @Valid @RequestBody EbaStep2PaymentRequestDTO request) {
        return ebaStep2PaymentService.executePayment(request)
                .map(ResponseEntity::ok);
    }



    @PostMapping("/step2/cancel")
    @Operation(summary = "Cancel an EBA STEP2 payment",
               description = """
               **Cancels an existing EBA STEP2 payment with Strong Customer Authentication (SCA) support.**

               The process:

               - Validates the cancellation request
               - Verifies the payment exists and is in a cancellable state
               - Verifies SCA if required (using the code delivered during cancellation simulation)
               - Attempts to cancel the payment with the EBA STEP2 network
               - Returns the cancellation status and any applicable fees

               **Important Notes:**
               - EBA STEP2 payments can typically be cancelled if they haven't been included in a processing cycle yet
               - Include the simulation reference from the /step2/cancel/simulate endpoint for SCA verification
               - Cancellation becomes more difficult once the payment has been included in a batch

               """)
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelEbaStep2Payment(
            @Valid @RequestBody EbaStep2CancellationRequestDTO request) {
        return ebaStep2PaymentService.cancelPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/cancel/simulate")
    @Operation(summary = "Simulate cancellation of an EBA STEP2 payment",
               description = """
               **Simulates cancellation of an EBA STEP2 payment to trigger SCA delivery and provide information.**

               The process:

               - Validates the cancellation request
               - Checks if the payment exists and is in a cancellable state
               - Determines if Strong Customer Authentication (SCA) is required for cancellation
               - Delivers SCA code if required (via SMS or other configured channel)
               - Returns information about cancellation feasibility, fees, and SCA requirements

               **Important Notes:**
               - The simulation reference returned should be included in the actual cancellation request
               - This endpoint does not actually cancel the payment
               - EBA STEP2 payments can typically be cancelled if they haven't been included in a processing cycle yet
               - Once a payment has been included in a batch, cancellation may not be possible

               """)
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateEbaStep2PaymentCancellation(
            @Valid @RequestBody EbaStep2CancellationRequestDTO request) {
        return ebaStep2PaymentService.simulateCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/schedule")
    @Operation(summary = "Schedule an EBA STEP2 payment", 
               description = """
               **Schedules an EBA STEP2 payment for future execution on the specified date.**

               This endpoint creates a scheduled payment instruction:

               - Validates the payment request data including IBAN and BIC
               - Verifies the execution date is valid (must be a future business day)
               - Verifies SCA if required
               - Creates a scheduled payment instruction
               - Supports optional recurrence patterns for recurring payments

               **Important Notes:**
               - The payment will be automatically submitted to the EBA STEP2 network on the specified date
               - Funds are not reserved until the actual execution date
               - EBA STEP2 only processes payments on business days
               - Payments scheduled for weekends or holidays will be processed on the next business day

               """)
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleEbaStep2Payment(
            @Valid @RequestBody EbaStep2PaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ebaStep2PaymentService.schedulePayment(request, executionDate)
                .map(ResponseEntity::ok);
    }
}
