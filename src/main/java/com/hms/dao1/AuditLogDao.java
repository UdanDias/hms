package com.hms.dao;

import com.hms.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogDao extends JpaRepository<AuditLogEntity, Long> {
}
