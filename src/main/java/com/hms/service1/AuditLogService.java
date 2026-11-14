package com.hms.service;

import com.hms.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
  AuditLogDTO getSelectedAuditLog(Long auditLogId);
  Page<AuditLogDTO> getAllAuditLogs(Pageable pageable);

  default String healthCheck() { return "AuditLog service is running"; }

  AuditLogDTO addAuditLog(AuditLogDTO dto);

  AuditLogDTO updateAuditLog(Long id, AuditLogDTO dto);

  void deleteAuditLog(Long id);
}
