package com.hms.controller;

import com.hms.dto.RefundDTO;
import com.hms.service.RefundService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService service;

    @GetMapping
    public String healthCheck() {
        return "Refund controller is running";
    }

    @GetMapping("/getselectedrefund")
    public ResponseEntity<RefundDTO> getSelectedRefund(@RequestParam("refundId") Long refundId) {
        return ResponseEntity.ok(service.getSelectedRefund(refundId));
    }

    @GetMapping("/getallrefunds")
    public ResponseEntity<List<RefundDTO>> getAllRefunds() {
        return ResponseEntity.ok(service.getAllRefunds());
    }

    @PostMapping("/addrefund")
    public ResponseEntity<RefundDTO> addRefund(@Valid @RequestBody RefundDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRefund(dto));
    }
}
