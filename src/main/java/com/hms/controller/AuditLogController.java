package com.hms.controller;

import com.hms.dto.AuditLogDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.AuditLogService;
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
@RequestMapping("/api/v1/auditlog")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String healthCheck() {
        return "AuditLog controller is running";
    }

    @GetMapping("/getselectedauditlog")
    public ResponseEntity<AuditLogDTO> getSelectedAuditLog(
            @RequestParam(name = "auditLogId") Long auditLogId) {
        if (auditLogId == null || auditLogId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AuditLogDTO result = auditLogService.getSelectedAuditLog(auditLogId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve AuditLog with ID {}", auditLogId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallauditlogs")
    public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs() {
        try {
            List<AuditLogDTO> results = auditLogService.getAllAuditLogs();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve AuditLogs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
