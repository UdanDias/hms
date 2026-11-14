package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.RoomRateDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomRateService;
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
 * HMS business service for RoomRate.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomRateServiceImpl implements RoomRateService {
    private final RoomRateDao roomRateDao;
    private final RoomTypeDao roomTypeDao;

    private RoomRate getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid RoomRate ID");
        return roomRateDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RoomRate not found: " + id));
    }

    private RoomRateDTO toDto(RoomRate entity) {
        RoomRateDTO dto = new RoomRateDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setRoomTypeId(entity.getRoomType() == null ? null : entity.getRoomType().getId());
        return dto;
    }

    private void apply(RoomRateDTO dto, RoomRate entity) {
        if (dto.getStayDate() != null) entity.setStayDate(dto.getStayDate());
        if (dto.getNightlyRate() != null) entity.setNightlyRate(dto.getNightlyRate());
        if (dto.getExtraAdultRate() != null) entity.setExtraAdultRate(dto.getExtraAdultRate());
        if (dto.getExtraChildRate() != null) entity.setExtraChildRate(dto.getExtraChildRate());
        if (dto.getMinimumNights() != null) entity.setMinimumNights(dto.getMinimumNights());
        if (dto.getStopSell() != null) entity.setStopSell(dto.getStopSell());
        if (dto.getRoomTypeId() != null) entity.setRoomType(
                    roomTypeDao.findById(dto.getRoomTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RoomType not found: " + dto.getRoomTypeId())));
    }

    @Override
    public RoomRateDTO getSelectedRoomRate(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<RoomRateDTO> getAllRoomRates(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return roomRateDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public RoomRateDTO addRoomRate(RoomRateDTO dto) {
        if (dto == null) throw new IllegalArgumentException("RoomRate DTO is required");
        if (dto.getRoomTypeId() == null || dto.getStayDate() == null) throw new IllegalArgumentException("Room type and stay date are required");
        RoomRate entity = new RoomRate();
        apply(dto, entity);
        validateRate(entity);
        entity = roomRateDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public RoomRateDTO updateRoomRate(Long id, RoomRateDTO dto) {
        if (dto == null) throw new IllegalArgumentException("RoomRate DTO is required");
        RoomRate entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        validateRate(entity);
        entity = roomRateDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteRoomRate(Long id) {
        RoomRate entity = getEntity(id);
        // Nothing extra.
        roomRateDao.delete(entity);
        roomRateDao.flush();
        // Nothing extra.
    }


    private void validateRate(RoomRate rate) {
        if (rate.getStayDate() == null) throw new IllegalArgumentException("Stay date required");
        if (rate.getMinimumNights() == null || rate.getMinimumNights() < 1)
            throw new IllegalArgumentException("Minimum nights must be at least 1");
        for (BigDecimal n : new BigDecimal[]{rate.getNightlyRate(),rate.getExtraAdultRate(),rate.getExtraChildRate()})
            if (n == null || n.signum() < 0) throw new IllegalArgumentException("Room rates must be nonnegative");
        if (rate.getStopSell() == null) rate.setStopSell(false);
    }

}
