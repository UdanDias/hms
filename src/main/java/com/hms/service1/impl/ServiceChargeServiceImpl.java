package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.ServiceChargeDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ServiceChargeService;
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
 * HMS business service for ServiceCharge.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceChargeServiceImpl implements ServiceChargeService {
    private final ServiceChargeDao serviceChargeDao;
    private final ReservationDao reservationDao;
    private final ReservationRoomDao reservationRoomDao;
    private final HotelServiceDao hotelServiceDao;
    private final UserDao userDao;

    private ServiceCharge getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid ServiceCharge ID");
        return serviceChargeDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ServiceCharge not found: " + id));
    }

    private ServiceChargeDTO toDto(ServiceCharge entity) {
        ServiceChargeDTO dto = new ServiceChargeDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setReservationId(entity.getReservation() == null ? null : entity.getReservation().getId());
        dto.setReservationRoomId(entity.getReservationRoom() == null ? null : entity.getReservationRoom().getId());
        dto.setServiceId(entity.getService() == null ? null : entity.getService().getId());
        dto.setRecordedById(entity.getRecordedBy() == null ? null : entity.getRecordedBy().getUserId());
        return dto;
    }

    private void apply(ServiceChargeDTO dto, ServiceCharge entity) {
        if (dto.getDescriptionSnapshot() != null) entity.setDescriptionSnapshot(dto.getDescriptionSnapshot());
        if (dto.getQuantity() != null) entity.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) entity.setUnitPrice(dto.getUnitPrice());
        if (dto.getDiscountAmount() != null) entity.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getTaxAmount() != null) entity.setTaxAmount(dto.getTaxAmount());
        if (dto.getProvidedAt() != null) entity.setProvidedAt(dto.getProvidedAt());
        if (dto.getReservationId() != null) entity.setReservation(
                    reservationDao.findById(dto.getReservationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId())));
        if (dto.getReservationRoomId() != null) entity.setReservationRoom(
                    reservationRoomDao.findById(dto.getReservationRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("ReservationRoom not found: " + dto.getReservationRoomId())));
        if (dto.getServiceId() != null) entity.setService(
                    hotelServiceDao.findById(dto.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelServiceEntity not found: " + dto.getServiceId())));
        if (dto.getRecordedById() != null) entity.setRecordedBy(
                    userDao.findById(dto.getRecordedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getRecordedById())));
    }

    @Override
    public ServiceChargeDTO getSelectedServiceCharge(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<ServiceChargeDTO> getAllServiceCharges(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return serviceChargeDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public ServiceChargeDTO addServiceCharge(ServiceChargeDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ServiceCharge DTO is required");
        // Nothing extra.
        ServiceCharge entity = new ServiceCharge();
        apply(dto, entity);
        entity.setStatus("RECORDED");
        if (entity.getProvidedAt() == null) entity.setProvidedAt(LocalDateTime.now(ZoneOffset.UTC));
        calculateCharge(entity);
        entity = serviceChargeDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public ServiceChargeDTO updateServiceCharge(Long id, ServiceChargeDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ServiceCharge DTO is required");
        ServiceCharge entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        if ("VOID".equalsIgnoreCase(entity.getStatus())) throw new IllegalStateException("Cannot edit a void charge");
        calculateCharge(entity);
        entity = serviceChargeDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteServiceCharge(Long id) {
        ServiceCharge entity = getEntity(id);
        if (!"VOID".equalsIgnoreCase(entity.getStatus())) throw new IllegalStateException("Void a charge instead of deleting an active one");
        serviceChargeDao.delete(entity);
        serviceChargeDao.flush();
        // Nothing extra.
    }


    private static BigDecimal zero(BigDecimal n) {return n == null ? BigDecimal.ZERO : n;}
    private void calculateCharge(ServiceCharge charge) {
        BigDecimal qty = zero(charge.getQuantity());
        BigDecimal unit = zero(charge.getUnitPrice());
        BigDecimal discount = zero(charge.getDiscountAmount());
        BigDecimal tax = zero(charge.getTaxAmount());
        for (BigDecimal n : new BigDecimal[]{qty,unit,discount,tax})
            if (n.signum() < 0) throw new IllegalArgumentException("Negative charge component");
        BigDecimal total = qty.multiply(unit).subtract(discount).add(tax);
        if (total.signum() < 0) throw new IllegalArgumentException("Discount exceeds gross charge");
        charge.setTotalAmount(total);
        if (charge.getService() == null || charge.getReservation() == null)
            throw new IllegalArgumentException("Service and reservation are required");
        if (!charge.getService().getHotel().getId().equals(charge.getReservation().getHotel().getId()))
            throw new IllegalArgumentException("Hotel service belongs to a different hotel");
        if (charge.getReservationRoom() != null &&
            !charge.getReservationRoom().getReservation().getId().equals(charge.getReservation().getId()))
            throw new IllegalArgumentException("Reservation room must belong to the reservation");
    }

    @Override @Transactional
    public ServiceChargeDTO voidServiceCharge(Long serviceChargeId) {
        ServiceCharge entity = getEntity(serviceChargeId);
        if ("VOID".equalsIgnoreCase(entity.getStatus())) return toDto(entity);
        entity.setStatus("VOID");
        return toDto(serviceChargeDao.saveAndFlush(entity));
    }

}
