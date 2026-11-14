package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.HotelServiceDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.HotelServiceManagementService;
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
 * HMS business service for HotelService.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceManagementServiceImpl implements HotelServiceManagementService {
    private final HotelServiceDao hotelServiceDao;
    private final HotelDao hotelDao;

    private HotelServiceEntity getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid HotelService ID");
        return hotelServiceDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HotelService not found: " + id));
    }

    private HotelServiceDTO toDto(HotelServiceEntity entity) {
        HotelServiceDTO dto = new HotelServiceDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        return dto;
    }

    private void apply(HotelServiceDTO dto, HotelServiceEntity entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getUnit() != null) entity.setUnit(dto.getUnit());
        if (dto.getDefaultPrice() != null) entity.setDefaultPrice(dto.getDefaultPrice());
        if (dto.getDefaultTaxRate() != null) entity.setDefaultTaxRate(dto.getDefaultTaxRate());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
    }

    @Override
    public HotelServiceDTO getSelectedHotelService(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<HotelServiceDTO> getAllHotelServices(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return hotelServiceDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public HotelServiceDTO addHotelService(HotelServiceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("HotelService DTO is required");
        // Nothing extra.
        HotelServiceEntity entity = new HotelServiceEntity();
        apply(dto, entity);
        if (entity.getIsActive() == null) entity.setIsActive(true);
        requireNonNegative(entity.getDefaultPrice(), "Default price");
        requireNonNegative(entity.getDefaultTaxRate(), "Default tax rate");
        entity = hotelServiceDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public HotelServiceDTO updateHotelService(Long id, HotelServiceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("HotelService DTO is required");
        HotelServiceEntity entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        requireNonNegative(entity.getDefaultPrice(), "Default price");
        requireNonNegative(entity.getDefaultTaxRate(), "Default tax rate");
        entity = hotelServiceDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteHotelService(Long id) {
        HotelServiceEntity entity = getEntity(id);
        // Nothing extra.
        hotelServiceDao.delete(entity);
        hotelServiceDao.flush();
        // Nothing extra.
    }


    private static void requireNonNegative(BigDecimal amount, String label) {
        if (amount == null || amount.signum() < 0)
            throw new IllegalArgumentException(label + " must be nonnegative");
    }

}
