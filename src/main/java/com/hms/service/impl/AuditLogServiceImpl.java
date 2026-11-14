package com.hms.service.impl;

import com.hms.dao.AuditLogDAO;
import com.hms.dto.AuditLogDTO;
import com.hms.entities.AuditLogEntity;
import com.hms.exception.AuditLogNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.AuditLogService;
import com.hms.util.EntityDTOConvert;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogDAO auditLogDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;

  @Override
  @Transactional(readOnly = true)
  public AuditLogDTO getSelectedAuditLog(Long auditLogId) {
    serviceSecurity.getCurrentUser();
    AuditLogEntity auditLogEntity =
        auditLogDao
            .findById(auditLogId)
            .orElseThrow(() -> new AuditLogNotFoundException("AuditLog not found"));
    serviceSecurity.checkRead(auditLogEntity);
    return entityDTOConvert.convertAuditLogEntityToAuditLogDTO(auditLogEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDTO> getAllAuditLogs() {
    serviceSecurity.getCurrentUser();
    List<AuditLogEntity> entities =
        auditLogDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertAuditLogEntityListToAuditLogDTOList(entities);
  }
}
