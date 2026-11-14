package com.hms.service;

import com.hms.dto.AuditLogDTO;
import java.util.List;

public interface AuditLogService {
  AuditLogDTO getSelectedAuditLog(Long auditLogId);

  List<AuditLogDTO> getAllAuditLogs();
}
