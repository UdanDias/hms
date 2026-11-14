package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.AuditLogDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * HMS business service for AuditLog.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogDao auditLogDao;
    private final HotelDao hotelDao;
    private final UserDao userDao;

    private AuditLogEntity getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid AuditLog ID");
        return auditLogDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found: " + id));
    }

    private AuditLogDTO toDto(AuditLogEntity entity) {
        AuditLogDTO dto = new AuditLogDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        dto.setActorUserId(entity.getActorUser() == null ? null : entity.getActorUser().getUserId());
        return dto;
    }

    private void apply(AuditLogDTO dto, AuditLogEntity entity) {
        if (dto.getAction() != null) entity.setAction(dto.getAction());
        if (dto.getEntityType() != null) entity.setEntityType(dto.getEntityType());
        if (dto.getEntityId() != null) entity.setEntityId(dto.getEntityId());
        if (dto.getOldValues() != null) entity.setOldValues(dto.getOldValues());
        if (dto.getNewValues() != null) entity.setNewValues(dto.getNewValues());
        if (dto.getRequestId() != null) entity.setRequestId(dto.getRequestId());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
        if (dto.getActorUserId() != null) entity.setActorUser(
                    userDao.findById(dto.getActorUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getActorUserId())));
    }

    @Override
    public AuditLogDTO getSelectedAuditLog(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return auditLogDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public AuditLogDTO addAuditLog(AuditLogDTO dto) {
        if (dto == null) throw new IllegalArgumentException("AuditLog DTO is required");
        if (dto.getAction() == null || dto.getAction().isBlank() || dto.getEntityType() == null || dto.getEntityId() == null) throw new IllegalArgumentException("Audit action, entity type and entity ID are required");
        AuditLogEntity entity = new AuditLogEntity();
        apply(dto, entity);
        entity.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));
        entity = auditLogDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public AuditLogDTO updateAuditLog(Long id, AuditLogDTO dto) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be edited");
    }

    @Override
    @Transactional
    public void deleteAuditLog(Long id) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }


}
