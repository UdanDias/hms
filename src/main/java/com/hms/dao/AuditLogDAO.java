package com.hms.dao;

import com.hms.dao.support.AbstractHibernateDAO;
import com.hms.entities.AuditLogEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for AuditLogEntity; business decisions are made by the service layer. */
@Repository
public class AuditLogDAO extends AbstractHibernateDAO<AuditLogEntity> {
    public AuditLogDAO() {
        super(AuditLogEntity.class, "id", AuditLogEntity::getId);
    }

    public List<AuditLogEntity> findByHotelId(Long hotelId) {
        positiveId(hotelId); return select("e.hotel.id = :p0", hotelId);
    }

    public List<AuditLogEntity> findByActorUserId(Long actorUserId) {
        positiveId(actorUserId); return select("e.actorUser.userId = :p0", actorUserId);
    }

    public List<AuditLogEntity> findByEntity(String entityType, Long entityId) {
        positiveId(entityId); return selectOrdered("e.entityType = :p0 and e.entityId = :p1", "e.occurredAt, e.id", entityType, entityId);
    }

    public List<AuditLogEntity> findByOccurredAtRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) throw new com.hms.exception.InvalidRequestException("End time must be after start time"); return selectOrdered("e.occurredAt >= :p0 and e.occurredAt < :p1", "e.occurredAt, e.id", start, end);
    }

}
