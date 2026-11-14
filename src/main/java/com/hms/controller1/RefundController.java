package com.hms.controller;

import com.hms.dto.RefundDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/refund")
@RequiredArgsConstructor
@Slf4j
public class RefundController {

    private final RefundService refundService;

    @GetMapping
    public String healthCheck() {
        return refundService.healthCheck();
    }

    @GetMapping("/getselectedrefund")
    public ResponseEntity<RefundDTO> getSelectedRefund(@RequestParam(name = "refundId") Long refundId) {
        if (refundId == null || refundId <= 0) return ResponseEntity.badRequest().build();
        try {
            RefundDTO result = refundService.getSelectedRefund(refundId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Refund with ID {}", refundId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallrefunds")
    public ResponseEntity<Page<RefundDTO>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(refundService.getAllRefunds(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve Refund records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addrefund")
    public ResponseEntity<RefundDTO> addRefund(@RequestBody RefundDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(refundService.addRefund(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Refund", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updaterefund")
    public ResponseEntity<RefundDTO> updateRefund(
            @RequestParam(name = "refundId") Long refundId,
            @RequestBody RefundDTO dto) {
        if (refundId == null || refundId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(refundService.updateRefund(refundId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Refund with ID {}", refundId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleterefund")
    public ResponseEntity<Void> deleteRefund(@RequestParam(name = "refundId") Long refundId) {
        if (refundId == null || refundId <= 0) return ResponseEntity.badRequest().build();
        try {
            refundService.deleteRefund(refundId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Refund with ID {}", refundId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
