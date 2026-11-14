package com.hms.controller;

import com.hms.dto.PaymentDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public String healthCheck() {
        return "Payment controller is running";
    }

    @GetMapping("/getselectedpayment")
    public ResponseEntity<PaymentDTO> getSelectedPayment(
            @RequestParam(name = "paymentId") Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            PaymentDTO result = paymentService.getSelectedPayment(paymentId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Payment with ID {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallpayments")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        try {
            List<PaymentDTO> results = paymentService.getAllPayments();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
