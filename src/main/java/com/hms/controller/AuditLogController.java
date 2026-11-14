package com.hms.controller;

import com.hms.dto.AuditLogDTO;
import com.hms.service.AuditLogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auditlog")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    @GetMapping
    public String healthCheck() {
        return "AuditLog controller is running";
    }

    @GetMapping("/getselectedauditlog")
    public ResponseEntity<AuditLogDTO> getSelectedAuditLog(@RequestParam("auditLogId") Long auditLogId) {
        return ResponseEntity.ok(service.getSelectedAuditLog(auditLogId));
    }

    @GetMapping("/getallauditlogs")
    public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(service.getAllAuditLogs());
    }
}
