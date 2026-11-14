package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.RoomTypeDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomTypeService;
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
 * HMS business service for RoomType.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeServiceImpl implements RoomTypeService {
    private final RoomTypeDao roomTypeDao;
    private final HotelDao hotelDao;

    private RoomType getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid RoomType ID");
        return roomTypeDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RoomType not found: " + id));
    }

    private RoomTypeDTO toDto(RoomType entity) {
        RoomTypeDTO dto = new RoomTypeDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        return dto;
    }

    private void apply(RoomTypeDTO dto, RoomType entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getMaxAdults() != null) entity.setMaxAdults(dto.getMaxAdults());
        if (dto.getMaxChildren() != null) entity.setMaxChildren(dto.getMaxChildren());
        if (dto.getMaxOccupancy() != null) entity.setMaxOccupancy(dto.getMaxOccupancy());
        if (dto.getBedDescription() != null) entity.setBedDescription(dto.getBedDescription());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
    }

    @Override
    public RoomTypeDTO getSelectedRoomType(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<RoomTypeDTO> getAllRoomTypes(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return roomTypeDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public RoomTypeDTO addRoomType(RoomTypeDTO dto) {
        if (dto == null) throw new IllegalArgumentException("RoomType DTO is required");
        // Nothing extra.
        RoomType entity = new RoomType();
        apply(dto, entity);
        if (entity.getIsActive() == null) entity.setIsActive(true);
        validateOccupancy(entity);
        entity = roomTypeDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public RoomTypeDTO updateRoomType(Long id, RoomTypeDTO dto) {
        if (dto == null) throw new IllegalArgumentException("RoomType DTO is required");
        RoomType entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        validateOccupancy(entity);
        entity = roomTypeDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteRoomType(Long id) {
        RoomType entity = getEntity(id);
        // Nothing extra.
        roomTypeDao.delete(entity);
        roomTypeDao.flush();
        // Nothing extra.
    }


    private void validateOccupancy(RoomType roomType) {
        if (roomType.getMaxAdults() == null || roomType.getMaxChildren() == null || roomType.getMaxOccupancy() == null)
            throw new IllegalArgumentException("Capacity limits required");
        if (roomType.getMaxAdults() < 0 || roomType.getMaxChildren() < 0 || roomType.getMaxOccupancy() < 1)
            throw new IllegalArgumentException("Invalid capacity limits");
        if (roomType.getMaxOccupancy() > roomType.getMaxAdults() + roomType.getMaxChildren())
            throw new IllegalArgumentException("Total capacity cannot exceed configured adult + child capacities");
    }

}
