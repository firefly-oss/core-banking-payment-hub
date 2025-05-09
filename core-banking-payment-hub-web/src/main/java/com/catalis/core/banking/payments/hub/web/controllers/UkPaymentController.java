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
    @Operation(summary = "Simulate a UK Faster Payment", description = "Simulates a UK Faster Payment without actual execution")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateFasterPayment(
            @Valid @RequestBody UkFasterPaymentRequestDTO request) {
        return ukPaymentService.simulateFasterPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/execute")
    @Operation(summary = "Execute a UK Faster Payment", description = "Executes a UK Faster Payment")
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeFasterPayment(
            @Valid @RequestBody UkFasterPaymentRequestDTO request) {
        return ukPaymentService.executeFasterPayment(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/fps/{paymentId}")
    @Operation(summary = "Cancel a UK Faster Payment", description = "Cancels an existing UK Faster Payment")
    @Deprecated
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelFasterPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true) @RequestParam("reason") @NotBlank String reason) {
        return ukPaymentService.cancelFasterPayment(paymentId, reason)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/cancel")
    @Operation(summary = "Cancel a UK Faster Payment", description = "Cancels an existing UK Faster Payment with SCA support")
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelFasterPayment(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.cancelFasterPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a UK Faster Payment",
               description = "Simulates cancellation of a UK Faster Payment to trigger SCA delivery and provide information")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateFasterPaymentCancellation(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.simulateFasterPaymentCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/fps/schedule")
    @Operation(summary = "Schedule a UK Faster Payment", description = "Schedules a UK Faster Payment for future execution")
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleFasterPayment(
            @Valid @RequestBody UkFasterPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ukPaymentService.scheduleFasterPayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // UK BACS endpoints

    @PostMapping("/bacs/simulate")
    @Operation(summary = "Simulate a UK BACS Payment", description = "Simulates a UK BACS Payment without actual execution")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateBacsPayment(
            @Valid @RequestBody UkBacsPaymentRequestDTO request) {
        return ukPaymentService.simulateBacsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/execute")
    @Operation(summary = "Execute a UK BACS Payment", description = "Executes a UK BACS Payment")
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeBacsPayment(
            @Valid @RequestBody UkBacsPaymentRequestDTO request) {
        return ukPaymentService.executeBacsPayment(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/bacs/{paymentId}")
    @Operation(summary = "Cancel a UK BACS Payment", description = "Cancels an existing UK BACS Payment")
    @Deprecated
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelBacsPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true) @RequestParam("reason") @NotBlank String reason) {
        return ukPaymentService.cancelBacsPayment(paymentId, reason)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/cancel")
    @Operation(summary = "Cancel a UK BACS Payment", description = "Cancels an existing UK BACS Payment with SCA support")
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelBacsPayment(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.cancelBacsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a UK BACS Payment",
               description = "Simulates cancellation of a UK BACS Payment to trigger SCA delivery and provide information")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateBacsPaymentCancellation(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.simulateBacsPaymentCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/bacs/schedule")
    @Operation(summary = "Schedule a UK BACS Payment", description = "Schedules a UK BACS Payment for future execution")
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleBacsPayment(
            @Valid @RequestBody UkBacsPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ukPaymentService.scheduleBacsPayment(request, executionDate)
                .map(ResponseEntity::ok);
    }

    // UK CHAPS endpoints

    @PostMapping("/chaps/simulate")
    @Operation(summary = "Simulate a UK CHAPS Payment", description = "Simulates a UK CHAPS Payment without actual execution")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateChapsPayment(
            @Valid @RequestBody UkChapsPaymentRequestDTO request) {
        return ukPaymentService.simulateChapsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/execute")
    @Operation(summary = "Execute a UK CHAPS Payment", description = "Executes a UK CHAPS Payment")
    public Mono<ResponseEntity<PaymentExecutionResultDTO>> executeChapsPayment(
            @Valid @RequestBody UkChapsPaymentRequestDTO request) {
        return ukPaymentService.executeChapsPayment(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/chaps/{paymentId}")
    @Operation(summary = "Cancel a UK CHAPS Payment", description = "Cancels an existing UK CHAPS Payment")
    @Deprecated
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelChapsPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancellation reason", required = true) @RequestParam("reason") @NotBlank String reason) {
        return ukPaymentService.cancelChapsPayment(paymentId, reason)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/cancel")
    @Operation(summary = "Cancel a UK CHAPS Payment", description = "Cancels an existing UK CHAPS Payment with SCA support")
    public Mono<ResponseEntity<PaymentCancellationResultDTO>> cancelChapsPayment(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.cancelChapsPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/cancel/simulate")
    @Operation(summary = "Simulate cancellation of a UK CHAPS Payment",
               description = "Simulates cancellation of a UK CHAPS Payment to trigger SCA delivery and provide information")
    public Mono<ResponseEntity<PaymentSimulationResultDTO>> simulateChapsPaymentCancellation(
            @Valid @RequestBody UkCancellationRequestDTO request) {
        return ukPaymentService.simulateChapsPaymentCancellation(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chaps/schedule")
    @Operation(summary = "Schedule a UK CHAPS Payment", description = "Schedules a UK CHAPS Payment for future execution")
    public Mono<ResponseEntity<PaymentScheduleResultDTO>> scheduleChapsPayment(
            @Valid @RequestBody UkChapsPaymentRequestDTO request,
            @Parameter(description = "Execution date (YYYY-MM-DD)", required = true) @RequestParam("executionDate") @NotBlank String executionDate) {
        return ukPaymentService.scheduleChapsPayment(request, executionDate)
                .map(ResponseEntity::ok);
    }
}
