package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.ReservationRoomEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for ReservationRoomEntity; business decisions are made by the service layer.
 */
@Repository
public class ReservationRoomDAO extends MutableHibernateDAO<ReservationRoomEntity> {
  public ReservationRoomDAO() {
    super(ReservationRoomEntity.class, "id", ReservationRoomEntity::getId);
  }

  public List<ReservationRoomEntity> findByReservationId(Long reservationId) {
    positiveId(reservationId);
    return select("e.reservation.id = :p0", reservationId);
  }

  public List<ReservationRoomEntity> findByRoomTypeId(Long roomTypeId) {
    positiveId(roomTypeId);
    return select("e.roomType.id = :p0", roomTypeId);
  }

  public List<ReservationRoomEntity> findByRoomId(Long roomId) {
    positiveId(roomId);
    return select("e.room.id = :p0", roomId);
  }

  public List<ReservationRoomEntity> findByAccommodationId(Long accommodationId) {
    positiveId(accommodationId);
    return select("e.accommodation.id = :p0", accommodationId);
  }

  public List<ReservationRoomEntity> findBlockingOverlapsByRoomId(
      Long roomId, LocalDate checkIn, LocalDate checkOut) {
    positiveId(roomId);
    dateRange(checkIn, checkOut);
    return select(
        "e.room.id = :p0 and e.status in ('confirmed','checked_in') and e.checkInDate < :p1 and"
            + " e.checkOutDate > :p2",
        roomId,
        checkOut,
        checkIn);
  }

  public List<ReservationRoomEntity> findBlockingOverlapsByRoomTypeId(
      Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
    positiveId(roomTypeId);
    dateRange(checkIn, checkOut);
    return select(
        "e.roomType.id = :p0 and e.status in ('confirmed','checked_in') and e.checkInDate < :p1 and"
            + " e.checkOutDate > :p2",
        roomTypeId,
        checkOut,
        checkIn);
  }

  public List<ReservationRoomEntity> findActiveStaysEndingAfter(Long p0, LocalDate p1) {
    return select(
        "e.roomType.id = :p0 and e.status in ('confirmed','checked_in') and"
            + " e.checkOutDate > :p1",
        p0,
        p1);
  }

  public List<ReservationRoomEntity> findActiveByRoomTypeId(Long p0) {
    return select("e.roomType.id = :p0 and e.status in ('confirmed','checked_in')", p0);
  }

  public List<ReservationRoomEntity> findNonCancelledByReservationId(Long p0) {
    return select("e.reservation.id = :p0 and e.status <> 'cancelled'", p0);
  }

  public List<ReservationRoomEntity> findBlockingStaysExcludingReservation(
      Long p0, Long p1, LocalDate p2, LocalDate p3) {
    return select(
        "e.roomType.id = :p0 and e.reservation.id <> :p1 and e.status in"
            + " ('confirmed','checked_in') and e.checkInDate < :p2 and e.checkOutDate > :p3",
        p0,
        p1,
        p2,
        p3);
  }

  public List<ReservationRoomEntity> findCheckedInOccupantsExcludingReservation(Long p0, Long p1) {
    return select(
        "e.room.id = :p0 and e.status = 'checked_in' and e.reservation.id <> :p1", p0, p1);
  }
}
