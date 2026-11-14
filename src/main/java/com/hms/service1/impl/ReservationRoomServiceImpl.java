package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.ReservationRoomDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationRoomService;
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
import java.time.temporal.ChronoUnit;

/**
 * HMS business service for ReservationRoom.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationRoomServiceImpl implements ReservationRoomService {
    private final ReservationRoomDao reservationRoomDao;
    private final ReservationDao reservationDao;
    private final RoomTypeDao roomTypeDao;
    private final RoomDao roomDao;
    private final AccommodationDao accommodationDao;
    private final BookingStatusHistoryDao bookingStatusHistoryDao;
    @PersistenceContext
    private EntityManager entityManager;

    private ReservationRoom getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid ReservationRoom ID");
        return reservationRoomDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ReservationRoom not found: " + id));
    }

    private ReservationRoomDTO toDto(ReservationRoom entity) {
        ReservationRoomDTO dto = new ReservationRoomDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setReservationId(entity.getReservation() == null ? null : entity.getReservation().getId());
        dto.setRoomTypeId(entity.getRoomType() == null ? null : entity.getRoomType().getId());
        dto.setRoomId(entity.getRoom() == null ? null : entity.getRoom().getId());
        dto.setAccommodationId(entity.getAccommodation() == null ? null : entity.getAccommodation().getId());
        return dto;
    }

    private void apply(ReservationRoomDTO dto, ReservationRoom entity) {
        if (dto.getCheckInDate() != null) entity.setCheckInDate(dto.getCheckInDate());
        if (dto.getCheckOutDate() != null) entity.setCheckOutDate(dto.getCheckOutDate());
        if (dto.getAdults() != null) entity.setAdults(dto.getAdults());
        if (dto.getChildren() != null) entity.setChildren(dto.getChildren());
        // Published RoomRate records, never request parameters, determine the nightly rate.
        if (dto.getExtraPersonTotal() != null) entity.setExtraPersonTotal(dto.getExtraPersonTotal());
        if (dto.getDiscountAmount() != null) entity.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getTaxAmount() != null) entity.setTaxAmount(dto.getTaxAmount());
        if (dto.getServiceChargeAmount() != null) entity.setServiceChargeAmount(dto.getServiceChargeAmount());
        if (dto.getCancellationPolicySnapshot() != null) entity.setCancellationPolicySnapshot(dto.getCancellationPolicySnapshot());
        if (dto.getReservationId() != null) entity.setReservation(
                    reservationDao.findById(dto.getReservationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId())));
        if (dto.getRoomTypeId() != null) entity.setRoomType(
                    roomTypeDao.findById(dto.getRoomTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RoomType not found: " + dto.getRoomTypeId())));
        if (dto.getRoomId() != null) entity.setRoom(
                    roomDao.findById(dto.getRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + dto.getRoomId())));
        if (dto.getAccommodationId() != null) entity.setAccommodation(
                    accommodationDao.findById(dto.getAccommodationId())
                        .orElseThrow(() -> new ResourceNotFoundException("AccommodationEntity not found: " + dto.getAccommodationId())));
    }

    @Override
    public ReservationRoomDTO getSelectedReservationRoom(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<ReservationRoomDTO> getAllReservationRooms(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return reservationRoomDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public ReservationRoomDTO addReservationRoom(ReservationRoomDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ReservationRoom DTO is required");
        if (dto.getReservationId() == null || dto.getRoomTypeId() == null) throw new IllegalArgumentException("Reservation and room type are required");
        ReservationRoom entity = new ReservationRoom();
        apply(dto, entity);
        if (entity.getStatus() == null) entity.setStatus("PENDING");
        validateStayAndPrice(entity);
        if (entity.getRoom() != null) checkAvailable(entity, entity.getRoom().getId());
        entity = reservationRoomDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public ReservationRoomDTO updateReservationRoom(Long id, ReservationRoomDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ReservationRoom DTO is required");
        ReservationRoom entity = getEntity(id);
        if (Set.of("CANCELLED", "CHECKED_OUT", "CHECKED_IN").contains(upper(entity.getStatus()))) throw new IllegalStateException("This reservation room is not editable");
        if (!"PENDING".equalsIgnoreCase(entity.getReservation().getStatus()) &&
            (dto.getCheckInDate() != null || dto.getCheckOutDate() != null ||
             dto.getRoomTypeId() != null || dto.getRoomId() != null || dto.getReservationId() != null))
            throw new IllegalStateException("Rebooking a confirmed reservation requires a controlled workflow");
        apply(dto, entity);
        validateStayAndPrice(entity);
        if (entity.getRoom() != null) checkAvailable(entity, entity.getRoom().getId());
        entity = reservationRoomDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteReservationRoom(Long id) {
        ReservationRoom entity = getEntity(id);
        if (!"PENDING".equalsIgnoreCase(entity.getStatus())) throw new IllegalStateException("Only pending reservation rooms can be deleted");
        reservationRoomDao.delete(entity);
        reservationRoomDao.flush();
        // Nothing extra.
    }


    private static String upper(String status) {return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);}
    private static BigDecimal zero(BigDecimal n) {return n == null ? BigDecimal.ZERO : n;}

    private void validateStayAndPrice(ReservationRoom rr) {
        if (rr.getCheckInDate() == null || rr.getCheckOutDate() == null || !rr.getCheckOutDate().isAfter(rr.getCheckInDate()))
            throw new IllegalArgumentException("Checkout must be after check-in");
        if (rr.getReservation() == null || rr.getRoomType() == null || rr.getAccommodation() == null)
            throw new IllegalArgumentException("Reservation, room type and accommodation are required");
        if (!rr.getAccommodation().getHotel().getId().equals(rr.getReservation().getHotel().getId()))
            throw new IllegalArgumentException("Accommodation belongs to another hotel");
        if (!Boolean.TRUE.equals(rr.getRoomType().getIsActive()))
            throw new IllegalStateException("Room type is inactive");
        long nights = ChronoUnit.DAYS.between(rr.getCheckInDate(), rr.getCheckOutDate());
        if (rr.getAdults() == null || rr.getAdults() < 1 || rr.getChildren() == null || rr.getChildren() < 0)
            throw new IllegalArgumentException("At least one adult and nonnegative children are required");
        if (rr.getRoomType() != null) {
            if (rr.getAdults() > rr.getRoomType().getMaxAdults() || rr.getChildren() > rr.getRoomType().getMaxChildren()
                 || rr.getAdults() + rr.getChildren() > rr.getRoomType().getMaxOccupancy())
                throw new IllegalArgumentException("Room occupancy exceeds room-type capacity");
            if (!rr.getRoomType().getHotel().getId().equals(rr.getReservation().getHotel().getId()))
                throw new IllegalArgumentException("Room type belongs to a different hotel");
        }
        // RoomRate entries, when configured, override client-supplied nightly pricing.
        List<RoomRate> rates = entityManager.createQuery(
            "select r from RoomRate r where r.roomType.id = :typeId and r.stayDate >= :start " +
            "and r.stayDate < :end order by r.stayDate", RoomRate.class)
            .setParameter("typeId", rr.getRoomType().getId())
            .setParameter("start", rr.getCheckInDate()).setParameter("end", rr.getCheckOutDate())
            .getResultList();
        Map<LocalDate,RoomRate> byDate = new HashMap<>();
        for (RoomRate rate : rates) byDate.put(rate.getStayDate(), rate);
        if (nights > 365) throw new IllegalArgumentException("Stay length exceeds 365 nights");
        BigDecimal roomCharge = BigDecimal.ZERO;
        // nightlyRate is a representative first-night rate; the total sums each day's rate.
        rr.setNightlyRate(null);
        for (LocalDate date = rr.getCheckInDate(); date.isBefore(rr.getCheckOutDate()); date = date.plusDays(1)) {
            RoomRate rate = byDate.get(date);
            if (rate == null) throw new IllegalStateException("No published nightly rate for " + date);
            if (Boolean.TRUE.equals(rate.getStopSell())) throw new IllegalStateException("Room sales stopped on " + date);
            if (rate.getMinimumNights() != null && nights < rate.getMinimumNights())
                throw new IllegalStateException("Minimum stay requirement not met on " + date);
            if (rate.getNightlyRate() == null || rate.getNightlyRate().signum() < 0)
                throw new IllegalStateException("Invalid published room rate for " + date);
            if (rr.getNightlyRate() == null) rr.setNightlyRate(rate.getNightlyRate());
            roomCharge = roomCharge.add(rate.getNightlyRate());
        }
        if (rr.getNightlyRate() == null) throw new IllegalArgumentException("Nightly rate is required");
        if (rr.getExtraPersonTotal() == null) rr.setExtraPersonTotal(BigDecimal.ZERO);
        if (rr.getDiscountAmount() == null) rr.setDiscountAmount(BigDecimal.ZERO);
        if (rr.getTaxAmount() == null) rr.setTaxAmount(BigDecimal.ZERO);
        if (rr.getServiceChargeAmount() == null) rr.setServiceChargeAmount(BigDecimal.ZERO);
        if (rr.getCancellationPolicySnapshot() == null) rr.setCancellationPolicySnapshot("");
        BigDecimal extra = zero(rr.getExtraPersonTotal());
        BigDecimal discount = zero(rr.getDiscountAmount());
        BigDecimal tax = zero(rr.getTaxAmount());
        BigDecimal charges = zero(rr.getServiceChargeAmount());
        for (BigDecimal val : new BigDecimal[]{extra,discount,tax,charges})
            if (val.signum() < 0) throw new IllegalArgumentException("Negative price component");
        BigDecimal total = roomCharge.add(extra).subtract(discount).add(tax).add(charges);
        if (rr.getAccommodation() != null) total = total.add(zero(rr.getAccommodation().getPrice()));
        if (total.signum() < 0) throw new IllegalArgumentException("Discount exceeds charges");
        rr.setTotalAmount(total);
    }

    private void checkAvailable(ReservationRoom rr, Long roomId) {
        Room room = entityManager.find(Room.class, roomId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + roomId);
        if (room.getRoomStatus() == null || room.getRoomStatus().getStatus() == null ||
            !"available".equalsIgnoreCase(room.getRoomStatus().getStatus().getValue()))
            throw new IllegalStateException("Room is unavailable or being serviced");
        if (!room.getRoomType().getId().equals(rr.getRoomType().getId()))
            throw new IllegalArgumentException("Assigned room is not the requested room type");
        if (!room.getHotel().getId().equals(rr.getReservation().getHotel().getId()))
            throw new IllegalArgumentException("Assigned room belongs to another hotel");
        Long count = entityManager.createQuery(
            "select count(r) from ReservationRoom r where r.room.id = :roomId and r.id <> :currentId " +
            "and upper(r.status) not in ('CANCELLED', 'VOID') " +
            "and r.checkInDate < :end and r.checkOutDate > :start", Long.class)
            .setParameter("roomId",roomId).setParameter("currentId",rr.getId() == null ? -1L : rr.getId())
            .setParameter("start",rr.getCheckInDate()).setParameter("end",rr.getCheckOutDate())
            .getSingleResult();
        if (count > 0) throw new IllegalStateException("Room already reserved for overlapping dates");
    }

    @Override @Transactional
    public ReservationRoomDTO assignRoom(Long reservationRoomId, Long roomId) {
        ReservationRoom rr = getEntity(reservationRoomId);
        if (Set.of("CANCELLED","CHECKED_OUT", "CHECKED_IN").contains(upper(rr.getStatus())) ||
            Set.of("CANCELLED","CHECKED_OUT", "CHECKED_IN").contains(upper(rr.getReservation().getStatus())))
            throw new IllegalStateException("Cannot assign rooms to a closed/occupied reservation");
        checkAvailable(rr,roomId);
        rr.setRoom(roomDao.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId)));
        return toDto(reservationRoomDao.saveAndFlush(rr));
    }

    @Override @Transactional
    public ReservationRoomDTO cancelReservationRoom(Long reservationRoomId, String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Reason required");
        ReservationRoom rr = getEntity(reservationRoomId);
        if (Set.of("CANCELLED","CHECKED_OUT", "CHECKED_IN").contains(upper(rr.getStatus())))
            throw new IllegalStateException("Reservation room already closed");
        String old = rr.getStatus();
        rr.setStatus("CANCELLED");
        reservationRoomDao.saveAndFlush(rr);
        BookingStatusHistoryEntity history = new BookingStatusHistoryEntity();
        history.setReservation(rr.getReservation());
        history.setReservationRoom(rr);
        history.setOldStatus(old);
        history.setNewStatus("CANCELLED");
        history.setReason(reason);
        history.setChangedAt(LocalDateTime.now(ZoneOffset.UTC));
        bookingStatusHistoryDao.save(history);
        return toDto(rr);
    }

}
