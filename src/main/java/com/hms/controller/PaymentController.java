package com.hms.controller;

import com.hms.dto.PaymentDTO;
import com.hms.dto.request.*;
import com.hms.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @GetMapping
    public String healthCheck() {
        return "Payment controller is running";
    }

    @GetMapping("/getselectedpayment")
    public ResponseEntity<PaymentDTO> getSelectedPayment(@RequestParam("paymentId") Long paymentId) {
        return ResponseEntity.ok(service.getSelectedPayment(paymentId));
    }

    @GetMapping("/getallpayments")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(service.getAllPayments());
    }

    @PostMapping("/addpayment")
    public ResponseEntity<PaymentDTO> addPayment(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPayment(dto));
    }

    @PatchMapping("/allocatepayment")
    public ResponseEntity<PaymentDTO> allocatePayment(@RequestParam("paymentId") Long paymentId, @Valid @RequestBody PaymentAllocationRequestDTO request) {
        return ResponseEntity.ok(service.allocatePayment(paymentId, request.getInvoiceId()));
    }
}
