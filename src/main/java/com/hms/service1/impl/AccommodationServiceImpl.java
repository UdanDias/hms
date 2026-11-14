package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.AccommodationDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.AccommodationService;
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
 * HMS business service for Accommodation.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationDao accommodationDao;
    private final HotelDao hotelDao;

    private AccommodationEntity getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Accommodation ID");
        return accommodationDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Accommodation not found: " + id));
    }

    private AccommodationDTO toDto(AccommodationEntity entity) {
        AccommodationDTO dto = new AccommodationDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        dto.setType(entity.getType() == null ? null : entity.getType().getValue());
        return dto;
    }

    private void apply(AccommodationDTO dto, AccommodationEntity entity) {
        if (dto.getType() != null) writeEnum(entity, dto.getType());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
    }

    @Override
    public AccommodationDTO getSelectedAccommodation(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<AccommodationDTO> getAllAccommodations(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return accommodationDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public AccommodationDTO addAccommodation(AccommodationDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Accommodation DTO is required");
        // Nothing extra.
        AccommodationEntity entity = new AccommodationEntity();
        apply(dto, entity);
        // Nothing extra.
        entity = accommodationDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public AccommodationDTO updateAccommodation(Long id, AccommodationDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Accommodation DTO is required");
        AccommodationEntity entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        // Nothing extra.
        entity = accommodationDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        AccommodationEntity entity = getEntity(id);
        // Nothing extra.
        accommodationDao.delete(entity);
        accommodationDao.flush();
        // Nothing extra.
    }



    private void writeEnum(AccommodationEntity entity, String rawValue) {
        if (rawValue == null) return;
        try {
            PropertyDescriptor property = new PropertyDescriptor("type", AccommodationEntity.class);
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
            throw new IllegalArgumentException("Invalid type value: " + rawValue, ex);
        }
    }
}
