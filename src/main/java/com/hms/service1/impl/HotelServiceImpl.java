package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.HotelDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.HotelService;
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
 * HMS business service for Hotel.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {
    private final HotelDao hotelDao;

    private HotelEntity getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Hotel ID");
        return hotelDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }

    private HotelDTO toDto(HotelEntity entity) {
        HotelDTO dto = new HotelDTO();
        BeanUtils.copyProperties(entity, dto);
        // Nothing extra.
        return dto;
    }

    private void apply(HotelDTO dto, HotelEntity entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getCity() != null) entity.setCity(dto.getCity());
        if (dto.getCountryCode() != null) entity.setCountryCode(dto.getCountryCode());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getTimezone() != null) entity.setTimezone(dto.getTimezone());
        if (dto.getCurrencyCode() != null) entity.setCurrencyCode(dto.getCurrencyCode());
        if (dto.getCheckInTime() != null) entity.setCheckInTime(dto.getCheckInTime());
        if (dto.getCheckOutTime() != null) entity.setCheckOutTime(dto.getCheckOutTime());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
    }

    @Override
    public HotelDTO getSelectedHotel(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public List<HotelDTO> getAllHotels( ) {
        return hotelDao.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public HotelDTO addHotel(HotelDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Hotel DTO is required");
        // Nothing extra.
        HotelEntity entity = new HotelEntity();
        apply(dto, entity);
        if (entity.getIsActive() == null) entity.setIsActive(true);
        validateHotel(entity);
        entity = hotelDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public HotelDTO updateHotel(Long id, HotelDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Hotel DTO is required");
        HotelEntity entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        validateHotel(entity);
        entity = hotelDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        HotelEntity entity = getEntity(id);
        // Nothing extra.
        hotelDao.delete(entity);
        hotelDao.flush();
        // Nothing extra.
    }


    private void validateHotel(HotelEntity hotel) {
        if (hotel.getTimezone() == null || hotel.getCurrencyCode() == null)
            throw new IllegalArgumentException("Hotel timezone and currency code are required");
        ZoneId.of(hotel.getTimezone()); // validate IANA timezone
        if (!hotel.getCurrencyCode().matches("[A-Za-z]{3}"))
            throw new IllegalArgumentException("Currency must be a three-letter code");
        hotel.setCurrencyCode(hotel.getCurrencyCode().toUpperCase(Locale.ROOT));
    }

}
