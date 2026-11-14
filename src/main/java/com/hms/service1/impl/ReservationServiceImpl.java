package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.ReservationDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationService;
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
 * HMS business service for Reservation.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    private final ReservationDao reservationDao;
    private final HotelDao hotelDao;
    private final CustomerDao customerDao;
    private final UserDao userDao;
    private final BookingStatusHistoryDao bookingStatusHistoryDao;
    @PersistenceContext
    private EntityManager entityManager;

    private Reservation getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Reservation ID");
        return reservationDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    private ReservationDTO toDto(Reservation entity) {
        ReservationDTO dto = new ReservationDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        dto.setBookerCustomerId(entity.getBookerCustomer() == null ? null : entity.getBookerCustomer().getId());
        dto.setCreatedById(entity.getCreatedBy() == null ? null : entity.getCreatedBy().getUserId());
        return dto;
    }

    private void apply(ReservationDTO dto, Reservation entity) {
        if (dto.getBookingReference() != null) entity.setBookingReference(dto.getBookingReference());
        if (dto.getBookingSource() != null) entity.setBookingSource(dto.getBookingSource());
        if (dto.getCurrencyCode() != null) entity.setCurrencyCode(dto.getCurrencyCode());
        if (dto.getSpecialRequests() != null) entity.setSpecialRequests(dto.getSpecialRequests());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
        if (dto.getBookerCustomerId() != null) entity.setBookerCustomer(
                    customerDao.findById(dto.getBookerCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("CustomerEntity not found: " + dto.getBookerCustomerId())));
        if (dto.getCreatedById() != null) entity.setCreatedBy(
                    userDao.findById(dto.getCreatedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getCreatedById())));
    }

    @Override
    public ReservationDTO getSelectedReservation(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<ReservationDTO> getAllReservations(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return reservationDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public ReservationDTO addReservation(ReservationDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Reservation DTO is required");
        if (dto.getBookingReference() == null || dto.getBookingReference().isBlank()) throw new IllegalArgumentException("Booking reference is required");
        Reservation entity = new Reservation();
        apply(dto, entity);
        if (entity.getHotel() == null || entity.getBookerCustomer() == null)
            throw new IllegalArgumentException("Reservation hotel and booker are required");
        if (!Boolean.TRUE.equals(entity.getHotel().getIsActive()))
            throw new IllegalStateException("Cannot book an inactive hotel");
        entity.setStatus("PENDING");
        entity.setConfirmedAt(null);
        entity.setCancelledAt(null);
        entity = reservationDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public ReservationDTO updateReservation(Long id, ReservationDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Reservation DTO is required");
        Reservation entity = getEntity(id);
        if (Set.of("CANCELLED", "CHECKED_OUT", "CHECKED_IN").contains(upper(entity.getStatus()))) throw new IllegalStateException("This reservation is not editable");
        if (!"PENDING".equalsIgnoreCase(entity.getStatus()) && dto.getHotelId() != null && !dto.getHotelId().equals(entity.getHotel().getId()))
            throw new IllegalStateException("Cannot move a confirmed reservation to another hotel");
        apply(dto, entity);
        // Nothing extra.
        entity = reservationDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        Reservation entity = getEntity(id);
        if (!"PENDING".equalsIgnoreCase(entity.getStatus())) throw new IllegalStateException("Only pending reservations can be deleted");
        reservationDao.delete(entity);
        reservationDao.flush();
        // Nothing extra.
    }


    private static final Set<String> CLOSED = Set.of("CANCELLED", "CHECKED_OUT");
    private static String upper(String s) {return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);}

    private void recordChange(Reservation reservation, String oldStatus, String newStatus, String reason) {
        BookingStatusHistoryEntity history = new BookingStatusHistoryEntity();
        history.setReservation(reservation);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setReason(reason);
        history.setChangedAt(LocalDateTime.now(ZoneOffset.UTC));
        // changedBy must be supplied by the authenticated principal when security is integrated.
        bookingStatusHistoryDao.save(history);
    }

    private ReservationDTO transition(Long id, String expected, String target, String reason) {
        Reservation reservation = getEntity(id);
        if (!expected.equalsIgnoreCase(reservation.getStatus()))
            throw new IllegalStateException("Expected " + expected + " status; actual " + reservation.getStatus());
        String old = reservation.getStatus();
        reservation.setStatus(target);
        if ("CONFIRMED".equals(target)) reservation.setConfirmedAt(LocalDateTime.now(ZoneOffset.UTC));
        if ("CANCELLED".equals(target)) {
            reservation.setCancelledAt(LocalDateTime.now(ZoneOffset.UTC));
            reservation.setCancellationReason(reason);
        }
        reservationDao.saveAndFlush(reservation);
        recordChange(reservation, old, target, reason);
        return toDto(reservation);
    }

    @Override @Transactional
    public ReservationDTO confirmReservation(Long reservationId) {
        Reservation reservation = getEntity(reservationId);
        List<ReservationRoom> rooms = entityManager.createQuery(
            "select rr from ReservationRoom rr where rr.reservation.id = :id", ReservationRoom.class)
            .setParameter("id",reservationId).getResultList();
        if (!Boolean.TRUE.equals(reservation.getHotel().getIsActive()))
            throw new IllegalStateException("Cannot confirm at an inactive hotel");
        if (rooms.isEmpty() || rooms.stream().noneMatch(rr -> !"CANCELLED".equalsIgnoreCase(rr.getStatus())))
            throw new IllegalStateException("Cannot confirm a reservation without active reserved rooms");
        if (!"PENDING".equalsIgnoreCase(reservation.getStatus()))
            throw new IllegalStateException("Only pending reservations can be confirmed");
        for (ReservationRoom room : rooms) {
            if ("PENDING".equalsIgnoreCase(room.getStatus())) room.setStatus("CONFIRMED");
        }
        return transition(reservationId, "PENDING", "CONFIRMED", null);
    }

    @Override @Transactional
    public ReservationDTO cancelReservation(Long reservationId, String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Cancellation reason is required");
        Reservation reservation = getEntity(reservationId);
        if (!Set.of("PENDING", "CONFIRMED").contains(upper(reservation.getStatus())))
            throw new IllegalStateException("Reservation already closed");
        String old = reservation.getStatus();
        reservation.setStatus("CANCELLED");
        reservation.setCancellationReason(reason);
        reservation.setCancelledAt(LocalDateTime.now(ZoneOffset.UTC));
        reservationDao.saveAndFlush(reservation);
        recordChange(reservation, old, "CANCELLED", reason);
        // Cancel associated room allocations as well.
        List<ReservationRoom> rooms = entityManager.createQuery(
            "select rr from ReservationRoom rr where rr.reservation.id = :id", ReservationRoom.class)
            .setParameter("id",reservationId).getResultList();
        for (ReservationRoom rr : rooms) {
            if (!"CHECKED_OUT".equalsIgnoreCase(rr.getStatus())) rr.setStatus("CANCELLED");
        }
        return toDto(reservation);
    }

    @Override @Transactional
    public ReservationDTO checkInReservation(Long reservationId) {
        Reservation reservation = getEntity(reservationId);
        if (!"CONFIRMED".equalsIgnoreCase(reservation.getStatus()))
            throw new IllegalStateException("Reservation must be confirmed before check-in");
        List<ReservationRoom> rooms = entityManager.createQuery(
            "select rr from ReservationRoom rr where rr.reservation.id = :id", ReservationRoom.class)
            .setParameter("id", reservationId).getResultList();
        if (rooms.isEmpty() || rooms.stream().anyMatch(rr -> rr.getRoom() == null || "CANCELLED".equalsIgnoreCase(rr.getStatus())))
            throw new IllegalStateException("Every active reservation room needs an assigned room");
        if (rooms.stream().anyMatch(rr -> rr.getRoom().getRoomStatus() == null ||
            rr.getRoom().getRoomStatus().getStatus() == null ||
            !"available".equalsIgnoreCase(rr.getRoom().getRoomStatus().getStatus().getValue())))
            throw new IllegalStateException("One or more assigned rooms are unavailable");
        LocalDate today = LocalDate.now(ZoneId.of(reservation.getHotel().getTimezone()));
        if (rooms.stream().anyMatch(rr -> today.isBefore(rr.getCheckInDate()) || !today.isBefore(rr.getCheckOutDate())))
            throw new IllegalStateException("Check-in must be within the booked stay dates");
        for (ReservationRoom rr : rooms) {
            rr.setStatus("CHECKED_IN");
            rr.setActualCheckInAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        return transition(reservationId, "CONFIRMED", "CHECKED_IN", null);
    }

    @Override @Transactional
    public ReservationDTO checkOutReservation(Long reservationId) {
        Reservation reservation = getEntity(reservationId);
        if (!"CHECKED_IN".equalsIgnoreCase(reservation.getStatus()))
            throw new IllegalStateException("Reservation must be checked in before checkout");
        List<ReservationRoom> rooms = entityManager.createQuery(
            "select rr from ReservationRoom rr where rr.reservation.id = :id", ReservationRoom.class)
            .setParameter("id",reservationId).getResultList();
        for (ReservationRoom rr : rooms) {
            if ("CHECKED_IN".equalsIgnoreCase(rr.getStatus())) {
                rr.setStatus("CHECKED_OUT");
                rr.setActualCheckOutAt(LocalDateTime.now(ZoneOffset.UTC));
            }
        }
        return transition(reservationId, "CHECKED_IN", "CHECKED_OUT", null);
    }

}
