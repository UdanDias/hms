package com.hms.service;

import com.hms.dto.AuditLogDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface AuditLogService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    AuditLogDTO getSelectedAuditLog(Long auditLogId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<AuditLogDTO> getAllAuditLogs();
}
