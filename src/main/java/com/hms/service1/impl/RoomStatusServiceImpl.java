package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.RoomStatusDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomStatusService;
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
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.*;

/**
 * HMS business service for RoomStatus.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomStatusServiceImpl implements RoomStatusService {
    private final RoomStatusDao roomStatusDao;

    private RoomStatus getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid RoomStatus ID");
        return roomStatusDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RoomStatus not found: " + id));
    }

    private RoomStatusDTO toDto(RoomStatus entity) {
        RoomStatusDTO dto = new RoomStatusDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setStatus(entity.getStatus() == null ? null : entity.getStatus().getValue());
        return dto;
    }

    private void apply(RoomStatusDTO dto, RoomStatus entity) {
        if (dto.getStatus() != null) writeEnum(entity, dto.getStatus());
    }

    @Override
    public RoomStatusDTO getSelectedRoomStatus(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<RoomStatusDTO> getAllRoomStatuses(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return roomStatusDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public RoomStatusDTO addRoomStatus(RoomStatusDTO dto) {
        if (dto == null) throw new IllegalArgumentException("RoomStatus DTO is required");
        // Nothing extra.
        RoomStatus entity = new RoomStatus();
        apply(dto, entity);
        // Nothing extra.
        entity = roomStatusDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public RoomStatusDTO updateRoomStatus(Long id, RoomStatusDTO dto) {
        if (dto == null) throw new IllegalArgumentException("RoomStatus DTO is required");
        RoomStatus entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        // Nothing extra.
        entity = roomStatusDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteRoomStatus(Long id) {
        RoomStatus entity = getEntity(id);
        // Nothing extra.
        roomStatusDao.delete(entity);
        roomStatusDao.flush();
        // Nothing extra.
    }



    private void writeEnum(RoomStatus entity, String rawValue) {
        if (rawValue == null) return;
        try {
            PropertyDescriptor property = new PropertyDescriptor("status", RoomStatus.class);
            Class<?> enumType = property.getPropertyType();
            Object enumValue;
            try {
                enumValue = enumType.getMethod("fromValue", String.class)
                    .invoke(null, rawValue.trim().toLowerCase(Locale.ROOT));
            } catch (NoSuchMethodException missingCustomParser) {
                enumValue = Enum.valueOf((Class)enumType.asSubclass(Enum.class),rawValue.trim().toUpperCase(Locale.ROOT));
            }
            property.getWriteMethod().invoke(entity, enumValue);
        } catch (ReflectiveOperationException | IntrospectionException ex) {
            throw new IllegalArgumentException("Invalid status value: " + rawValue, ex);
        }
    }
}
