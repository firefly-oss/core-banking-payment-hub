package com.catalis.core.banking.payments.hub.web.controllers;

import com.catalis.core.banking.payments.hub.core.services.EbaStep2PaymentService;
import com.catalis.core.banking.payments.hub.core.services.Target2PaymentService;
import com.catalis.core.banking.payments.hub.core.services.TipsPaymentService;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentCancellationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentExecutionResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentScheduleResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.common.PaymentSimulationResultDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.EbaStep2CancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.EbaStep2PaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.Target2CancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.Target2PaymentRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.TipsCancellationRequestDTO;
import com.catalis.core.banking.payments.hub.interfaces.dtos.european.TipsPaymentRequestDTO;
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
    @Operation(summary = "Simulate a TARGET2 payment", description = "Simulates a TARGET2 payment without actual execution")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTarget2Payment(
            @Valid @RequestBody Target2PaymentRequestDTO request) {
        return target2PaymentService.simulatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/execute")
    @Operation(summary = "Execute a TARGET2 payment", description = "Executes a TARGET2 payment")
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeTarget2Payment(
            @Valid @RequestBody Target2PaymentRequestDTO request) {
        return target2PaymentService.executePayment(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/target2/{paymentId}")
    @Operation(summary = "Cancel a TARGET2 payment", description = "Cancels an existing TARGET2 payment")
    @Deprecated
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelTarget2Payment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true) @RequestParam("reason") @NotBlank String reason) {
        return target2PaymentService.cancelPayment(paymentId, reason)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/cancel")
    @Operation(summary = "Cancel a TARGET2 payment", description = "Cancels an existing TARGET2 payment with SCA support")
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelTarget2Payment(
            @Valid @RequestBody Target2CancellationRequestDTO request) {
        return target2PaymentService.cancelPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a TARGET2 payment",
               description = "Simulates cancellation of a TARGET2 payment to trigger SCA delivery and provide information")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTarget2PaymentCancellation(
            @Valid @RequestBody Target2CancellationRequestDTO request) {
        return target2PaymentService.simulateCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/target2/schedule")
    @Operation(summary = "Schedule a TARGET2 payment", description = "Schedules a TARGET2 payment for future execution")
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleTarget2Payment(
            @Valid @RequestBody Target2PaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return target2PaymentService.schedulePayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // TIPS endpoints

    @PostMapping("/tips/simulate")
    @Operation(summary = "Simulate a TIPS payment", description = "Simulates a TIPS payment without actual execution")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTipsPayment(
            @Valid @RequestBody TipsPaymentRequestDTO request) {
        return tipsPaymentService.simulatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/execute")
    @Operation(summary = "Execute a TIPS payment", description = "Executes a TIPS payment")
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeTipsPayment(
            @Valid @RequestBody TipsPaymentRequestDTO request) {
        return tipsPaymentService.executePayment(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/tips/{paymentId}")
    @Operation(summary = "Cancel a TIPS payment", description = "Cancels an existing TIPS payment")
    @Deprecated
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelTipsPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true) @RequestParam("reason") @NotBlank String reason) {
        return tipsPaymentService.cancelPayment(paymentId, reason)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/cancel")
    @Operation(summary = "Cancel a TIPS payment", description = "Cancels an existing TIPS payment with SCA support")
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelTipsPayment(
            @Valid @RequestBody TipsCancellationRequestDTO request) {
        return tipsPaymentService.cancelPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a TIPS payment",
               description = "Simulates cancellation of a TIPS payment to trigger SCA delivery and provide information")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateTipsPaymentCancellation(
            @Valid @RequestBody TipsCancellationRequestDTO request) {
        return tipsPaymentService.simulateCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/tips/schedule")
    @Operation(summary = "Schedule a TIPS payment", description = "Schedules a TIPS payment for future execution")
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleTipsPayment(
            @Valid @RequestBody TipsPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return tipsPaymentService.schedulePayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // EBA STEP2 endpoints

    @PostMapping("/step2/simulate")
    @Operation(summary = "Simulate an EBA STEP2 payment", description = "Simulates an EBA STEP2 payment without actual execution")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateEbaStep2Payment(
            @Valid @RequestBody EbaStep2PaymentRequestDTO request) {
        return ebaStep2PaymentService.simulatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/execute")
    @Operation(summary = "Execute an EBA STEP2 payment", description = "Executes an EBA STEP2 payment")
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeEbaStep2Payment(
            @Valid @RequestBody EbaStep2PaymentRequestDTO request) {
        return ebaStep2PaymentService.executePayment(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/step2/{paymentId}")
    @Operation(summary = "Cancel an EBA STEP2 payment", description = "Cancels an existing EBA STEP2 payment")
    @Deprecated
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelEbaStep2Payment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true) @RequestParam("reason") @NotBlank String reason) {
        return ebaStep2PaymentService.cancelPayment(paymentId, reason)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/cancel")
    @Operation(summary = "Cancel an EBA STEP2 payment", description = "Cancels an existing EBA STEP2 payment with SCA support")
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelEbaStep2Payment(
            @Valid @RequestBody EbaStep2CancellationRequestDTO request) {
        return ebaStep2PaymentService.cancelPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/cancel/simulate")
    @Operation(summary = "Simulate cancellation of an EBA STEP2 payment",
               description = "Simulates cancellation of an EBA STEP2 payment to trigger SCA delivery and provide information")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateEbaStep2PaymentCancellation(
            @Valid @RequestBody EbaStep2CancellationRequestDTO request) {
        return ebaStep2PaymentService.simulateCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/step2/schedule")
    @Operation(summary = "Schedule an EBA STEP2 payment", description = "Schedules an EBA STEP2 payment for future execution")
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleEbaStep2Payment(
            @Valid @RequestBody EbaStep2PaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ebaStep2PaymentService.schedulePayment(request, executionDate)
                .map(ResponseEntity::ok);
    }
}
