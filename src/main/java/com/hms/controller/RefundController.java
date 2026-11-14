package com.hms.controller;

import com.hms.dto.RefundDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RefundService;
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
@RequestMapping("/api/v1/refund")
@RequiredArgsConstructor
@Slf4j
public class RefundController {

    private final RefundService refundService;

    @GetMapping
    public String healthCheck() {
        return "Refund controller is running";
    }

    @GetMapping("/getselectedrefund")
    public ResponseEntity<RefundDTO> getSelectedRefund(
            @RequestParam(name = "refundId") Long refundId) {
        if (refundId == null || refundId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RefundDTO result = refundService.getSelectedRefund(refundId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Refund with ID {}", refundId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallrefunds")
    public ResponseEntity<List<RefundDTO>> getAllRefunds() {
        try {
            List<RefundDTO> results = refundService.getAllRefunds();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Refunds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
