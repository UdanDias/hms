package com.hms.service.impl;

import com.hms.dao.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.hms.dto.ReservationCustomerDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationCustomerService;
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
 * HMS business service for ReservationCustomer.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationCustomerServiceImpl implements ReservationCustomerService {
    private final ReservationCustomerDao reservationCustomerDao;
    @PersistenceContext
    private EntityManager entityManager;
    private final ReservationRoomDao reservationRoomDao;
    private final CustomerDao customerDao;

    private ReservationCustomer getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid ReservationCustomer ID");
        return reservationCustomerDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ReservationCustomer not found: " + id));
    }

    private ReservationCustomerDTO toDto(ReservationCustomer entity) {
        ReservationCustomerDTO dto = new ReservationCustomerDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setReservationRoomId(entity.getReservationRoom() == null ? null : entity.getReservationRoom().getId());
        dto.setCustomerId(entity.getCustomer() == null ? null : entity.getCustomer().getId());
        return dto;
    }

    private void apply(ReservationCustomerDTO dto, ReservationCustomer entity) {
        if (dto.getIsPrimaryCustomer() != null) entity.setIsPrimaryCustomer(dto.getIsPrimaryCustomer());
        if (dto.getReservationRoomId() != null) entity.setReservationRoom(
                    reservationRoomDao.findById(dto.getReservationRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("ReservationRoom not found: " + dto.getReservationRoomId())));
        if (dto.getCustomerId() != null) entity.setCustomer(
                    customerDao.findById(dto.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("CustomerEntity not found: " + dto.getCustomerId())));
    }

    @Override
    public ReservationCustomerDTO getSelectedReservationCustomer(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<ReservationCustomerDTO> getAllReservationCustomers(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return reservationCustomerDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public ReservationCustomerDTO addReservationCustomer(ReservationCustomerDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ReservationCustomer DTO is required");
        // Nothing extra.
        ReservationCustomer entity = new ReservationCustomer();
        apply(dto, entity);
        if (entity.getIsPrimaryCustomer() == null) entity.setIsPrimaryCustomer(false);
        validatePrimaryCustomer(entity);
        entity = reservationCustomerDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public ReservationCustomerDTO updateReservationCustomer(Long id, ReservationCustomerDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ReservationCustomer DTO is required");
        ReservationCustomer entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        // Nothing extra.
        validatePrimaryCustomer(entity);
        entity = reservationCustomerDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteReservationCustomer(Long id) {
        ReservationCustomer entity = getEntity(id);
        // Nothing extra.
        reservationCustomerDao.delete(entity);
        reservationCustomerDao.flush();
        // Nothing extra.
    }


    private void validatePrimaryCustomer(ReservationCustomer entity) {
        if (!Boolean.TRUE.equals(entity.getIsPrimaryCustomer())) return;
        if (entity.getReservationRoom() == null)
            throw new IllegalArgumentException("Reservation room is required for a primary guest");
        Long existing = entityManager.createQuery(
            "select count(rc) from ReservationCustomer rc " +
            "where rc.reservationRoom.id = :roomId and rc.isPrimaryCustomer = true " +
            "and rc.id <> :currentId", Long.class)
            .setParameter("roomId", entity.getReservationRoom().getId())
            .setParameter("currentId", entity.getId() == null ? -1L : entity.getId())
            .getSingleResult();
        if (existing > 0) throw new IllegalStateException("Only one primary guest is allowed per reservation room");
    }

}
