package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.BookingStatusHistoryDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.BookingStatusHistoryService;
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
 * HMS business service for BookingStatusHistory.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingStatusHistoryServiceImpl implements BookingStatusHistoryService {
    private final BookingStatusHistoryDao bookingStatusHistoryDao;
    private final ReservationDao reservationDao;
    private final ReservationRoomDao reservationRoomDao;
    private final UserDao userDao;

    private BookingStatusHistoryEntity getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid BookingStatusHistory ID");
        return bookingStatusHistoryDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BookingStatusHistory not found: " + id));
    }

    private BookingStatusHistoryDTO toDto(BookingStatusHistoryEntity entity) {
        BookingStatusHistoryDTO dto = new BookingStatusHistoryDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setReservationId(entity.getReservation() == null ? null : entity.getReservation().getId());
        dto.setReservationRoomId(entity.getReservationRoom() == null ? null : entity.getReservationRoom().getId());
        dto.setChangedById(entity.getChangedBy() == null ? null : entity.getChangedBy().getUserId());
        return dto;
    }

    private void apply(BookingStatusHistoryDTO dto, BookingStatusHistoryEntity entity) {
        if (dto.getOldStatus() != null) entity.setOldStatus(dto.getOldStatus());
        if (dto.getNewStatus() != null) entity.setNewStatus(dto.getNewStatus());
        if (dto.getReason() != null) entity.setReason(dto.getReason());
        if (dto.getReservationId() != null) entity.setReservation(
                    reservationDao.findById(dto.getReservationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId())));
        if (dto.getReservationRoomId() != null) entity.setReservationRoom(
                    reservationRoomDao.findById(dto.getReservationRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("ReservationRoom not found: " + dto.getReservationRoomId())));
        if (dto.getChangedById() != null) entity.setChangedBy(
                    userDao.findById(dto.getChangedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getChangedById())));
    }

    @Override
    public BookingStatusHistoryDTO getSelectedBookingStatusHistory(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<BookingStatusHistoryDTO> getAllBookingStatusHistories(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return bookingStatusHistoryDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public BookingStatusHistoryDTO addBookingStatusHistory(BookingStatusHistoryDTO dto) {
        if (dto == null) throw new IllegalArgumentException("BookingStatusHistory DTO is required");
        if (dto.getReservationId() == null || dto.getNewStatus() == null) throw new IllegalArgumentException("Reservation ID and new status are required");
        BookingStatusHistoryEntity entity = new BookingStatusHistoryEntity();
        apply(dto, entity);
        entity.setChangedAt(LocalDateTime.now(ZoneOffset.UTC));
        entity = bookingStatusHistoryDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public BookingStatusHistoryDTO updateBookingStatusHistory(Long id, BookingStatusHistoryDTO dto) {
        throw new UnsupportedOperationException("Booking status history are append-only and cannot be edited");
    }

    @Override
    @Transactional
    public void deleteBookingStatusHistory(Long id) {
        throw new UnsupportedOperationException("Booking status history are append-only and cannot be deleted");
    }


}
