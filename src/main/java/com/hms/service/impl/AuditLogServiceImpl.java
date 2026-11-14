package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public AuditLogDTO getSelectedAuditLog(Long auditLogId) {
    s.actor();
    var e = dao.require(AuditLogEntity.class, auditLogId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<AuditLogDTO> getAllAuditLogs() {
    s.actor();
    return dao.all(AuditLogEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }
}
