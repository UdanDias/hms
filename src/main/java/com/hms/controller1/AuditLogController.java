package com.hms.controller;

import com.hms.dto.AuditLogDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/auditlog")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String healthCheck() {
        return auditLogService.healthCheck();
    }

    @GetMapping("/getselectedauditlog")
    public ResponseEntity<AuditLogDTO> getSelectedAuditLog(@RequestParam(name = "auditLogId") Long auditLogId) {
        if (auditLogId == null || auditLogId <= 0) return ResponseEntity.badRequest().build();
        try {
            AuditLogDTO result = auditLogService.getSelectedAuditLog(auditLogId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve AuditLog with ID {}", auditLogId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallauditlogs")
    public ResponseEntity<Page<AuditLogDTO>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(auditLogService.getAllAuditLogs(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve AuditLog records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addauditlog")
    public ResponseEntity<AuditLogDTO> addAuditLog(@RequestBody AuditLogDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(auditLogService.addAuditLog(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add AuditLog", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateauditlog")
    public ResponseEntity<AuditLogDTO> updateAuditLog(
            @RequestParam(name = "auditLogId") Long auditLogId,
            @RequestBody AuditLogDTO dto) {
        if (auditLogId == null || auditLogId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(auditLogService.updateAuditLog(auditLogId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update AuditLog with ID {}", auditLogId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteauditlog")
    public ResponseEntity<Void> deleteAuditLog(@RequestParam(name = "auditLogId") Long auditLogId) {
        if (auditLogId == null || auditLogId <= 0) return ResponseEntity.badRequest().build();
        try {
            auditLogService.deleteAuditLog(auditLogId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete AuditLog with ID {}", auditLogId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
