package com.hms.service.impl.support;

import static com.hms.service.impl.support.ServiceSupport.money;

import com.hms.dao.HmsDao;
import com.hms.entities.*;
import com.hms.entities.enums.RoomStatusValue;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingRules {
  private final HmsDao dao;
  private final ServiceSupport s;

  public List<ReservationRoomEntity> rooms(Long id) {
    return dao.query(
        ReservationRoomEntity.class, "e.reservation.id = :p0 and e.status <> 'cancelled'", id);
  }

  public void draft(ReservationEntity r) {
    s.require("pending".equals(r.getStatus()), "Only pending reservations can be edited");
  }

  public void prepare(ReservationEntity e, ReservationEntity old) {
    if (e.getSpecialRequests() == null) e.setSpecialRequests("");
    s.require(
        e.getHotel() != null && Boolean.TRUE.equals(e.getHotel().getIsActive()),
        "An active hotel is required");
    s.require(e.getBookerCustomer() != null, "Booker customer is required");
    if (old == null) {
      e.setStatus("pending");
      e.setBookingReference(s.number("RES"));
      e.setCreatedBy(s.actor());
      e.setConfirmedAt(null);
      e.setCancelledAt(null);
      e.setCancellationReason(null);
    } else {
      draft(old);
      s.same(e.getHotel().getId(), old.getHotel().getId(), "Hotel");
      e.setStatus(old.getStatus());
      e.setBookingReference(old.getBookingReference());
      e.setCreatedBy(old.getCreatedBy());
      e.setConfirmedAt(old.getConfirmedAt());
      e.setCancelledAt(old.getCancelledAt());
      e.setCancellationReason(old.getCancellationReason());
    }
    e.setCurrencyCode(e.getHotel().getCurrencyCode());
    s.text(e.getBookingSource(), "Booking source");
  }

  public void prepareRoom(ReservationRoomEntity e, ReservationRoomEntity old) {
    s.require(
        e.getReservation() != null && e.getRoomType() != null && e.getAccommodation() != null,
        "Reservation, room type and accommodation are required");
    draft(e.getReservation());
    if (old != null) {
      s.require("pending".equals(old.getStatus()), "Cancelled room reservations cannot be edited");
      draft(old.getReservation());
      s.same(old.getReservation().getId(), e.getReservation().getId(), "Reservation");
    }
    var hotel = e.getReservation().getHotel();
    s.same(hotel.getId(), e.getRoomType().getHotel().getId(), "Room type");
    s.same(hotel.getId(), e.getAccommodation().getHotel().getId(), "Accommodation");
    s.require(Boolean.TRUE.equals(e.getRoomType().getIsActive()), "Room type is inactive");
    s.require(
        e.getCheckInDate() != null
            && e.getCheckOutDate() != null
            && e.getCheckOutDate().isAfter(e.getCheckInDate()),
        "Checkout must be after check-in");
    s.require(!e.getCheckInDate().isBefore(s.today(hotel)), "Cannot create a stay in the past");
    s.require(
        ChronoUnit.DAYS.between(e.getCheckInDate(), e.getCheckOutDate()) <= 366,
        "Maximum stay is 366 nights");
    int a = e.getAdults() == null ? 0 : e.getAdults(),
        c = e.getChildren() == null ? 0 : e.getChildren();
    s.require(
        a >= 1
            && c >= 0
            && a <= e.getRoomType().getMaxAdults()
            && c <= e.getRoomType().getMaxChildren()
            && a + c <= e.getRoomType().getMaxOccupancy(),
        "Occupancy exceeds room-type limits");
    e.setAdults(a);
    e.setChildren(c);
    checkAssignedRoom(e);
    e.setNightlyRate(
        s.nonnegative(
            e.getNightlyRate() == null ? e.getAccommodation().getPrice() : e.getNightlyRate()));
    e.setExtraPersonTotal(s.nonnegative(e.getExtraPersonTotal()));
    e.setDiscountAmount(s.nonnegative(e.getDiscountAmount()));
    e.setTaxAmount(s.nonnegative(e.getTaxAmount()));
    e.setServiceChargeAmount(s.nonnegative(e.getServiceChargeAmount()));
    BigDecimal base =
        e.getNightlyRate()
            .multiply(
                BigDecimal.valueOf(
                    ChronoUnit.DAYS.between(e.getCheckInDate(), e.getCheckOutDate())))
            .add(e.getExtraPersonTotal());
    s.require(e.getDiscountAmount().compareTo(base) <= 0, "Discount exceeds room charge");
    e.setTotalAmount(
        money(
            base.subtract(e.getDiscountAmount())
                .add(e.getTaxAmount())
                .add(e.getServiceChargeAmount())));
    e.setStatus("pending");
    e.setActualCheckInAt(null);
    e.setActualCheckOutAt(null);
    if (e.getCancellationPolicySnapshot() == null) e.setCancellationPolicySnapshot("");
    restrictions(e);
  }

  public void checkAssignedRoom(ReservationRoomEntity e) {
    if (e.getRoom() == null) return;
    var r = e.getRoom();
    s.same(r.getHotel().getId(), e.getReservation().getHotel().getId(), "Room");
    s.same(r.getRoomType().getId(), e.getRoomType().getId(), "Room type");
    s.require(sellable(r), "Assigned room is unavailable or servicing");
  }

  private boolean sellable(RoomEntity r) {
    return "active".equals(r.getOperationalStatus())
        && r.getRoomStatus().getStatus() == RoomStatusValue.AVAILABLE;
  }

  private void restrictions(ReservationRoomEntity e) {
    long nights = ChronoUnit.DAYS.between(e.getCheckInDate(), e.getCheckOutDate());
    for (var rate :
        dao.query(
            RoomRateEntity.class,
            "e.roomType.id = :p0 and e.stayDate >= :p1 and e.stayDate < :p2",
            e.getRoomType().getId(),
            e.getCheckInDate(),
            e.getCheckOutDate())) {
      s.require(
          !Boolean.TRUE.equals(rate.getStopSell()),
          "Room type is closed for sale on " + rate.getStayDate());
      s.require(nights >= rate.getMinimumNights(), "Minimum stay requirement is not met");
    }
  }

  public void available(ReservationEntity reservation) {
    if ("pending".equals(reservation.getStatus()))
      s.require(Boolean.TRUE.equals(reservation.getHotel().getIsActive()), "Hotel is inactive");
    var requested = rooms(reservation.getId());
    s.require(!requested.isEmpty(), "Add at least one room before confirmation");
    for (var wanted : requested) {
      checkAssignedRoom(wanted);
      if ("pending".equals(reservation.getStatus()))
        s.require(Boolean.TRUE.equals(wanted.getRoomType().getIsActive()), "Room type is inactive");
      if ("pending".equals(reservation.getStatus())) restrictions(wanted);
      long capacity =
          dao.query(RoomEntity.class, "e.roomType.id = :p0", wanted.getRoomType().getId()).stream()
              .filter(this::sellable)
              .count();
      var occupied =
          dao.query(
              ReservationRoomEntity.class,
              "e.roomType.id = :p0 and e.reservation.id <> :p1 and e.status in"
                  + " ('confirmed','checked_in') and e.checkInDate < :p2 and e.checkOutDate > :p3",
              wanted.getRoomType().getId(),
              reservation.getId(),
              wanted.getCheckOutDate(),
              wanted.getCheckInDate());
      for (LocalDate day = wanted.getCheckInDate();
          day.isBefore(wanted.getCheckOutDate());
          day = day.plusDays(1)) {
        final LocalDate d = day;
        long need =
            requested.stream()
                .filter(
                    x ->
                        x.getRoomType().getId().equals(wanted.getRoomType().getId())
                            && covers(x, d))
                .count();
        long taken = occupied.stream().filter(x -> covers(x, d)).count();
        s.require(need + taken <= capacity, "No room-type capacity on " + day);
      }
      if (wanted.getRoom() != null) {
        s.require(
            occupied.stream()
                .noneMatch(
                    x ->
                        x.getRoom() != null
                            && x.getRoom().getId().equals(wanted.getRoom().getId())),
            "Assigned room overlaps another booking");
        s.require(
            requested.stream()
                .noneMatch(
                    x ->
                        !x.getId().equals(wanted.getId())
                            && x.getRoom() != null
                            && x.getRoom().getId().equals(wanted.getRoom().getId())
                            && overlap(x, wanted)),
            "The same room is assigned twice for overlapping dates");
      }
    }
  }

  private boolean covers(ReservationRoomEntity r, LocalDate day) {
    return !day.isBefore(r.getCheckInDate()) && day.isBefore(r.getCheckOutDate());
  }

  private boolean overlap(ReservationRoomEntity a, ReservationRoomEntity b) {
    return a.getCheckInDate().isBefore(b.getCheckOutDate())
        && b.getCheckInDate().isBefore(a.getCheckOutDate());
  }

  public void confirm(ReservationEntity r) {
    draft(r);
    available(r);
    r.setConfirmedAt(s.now());
    transition(r, "confirmed", null);
  }

  public void cancel(ReservationEntity r, String reason) {
    s.require(
        Set.of("pending", "confirmed").contains(r.getStatus()),
        "Only pending or confirmed reservations can be cancelled");
    s.text(reason, "Cancellation reason");
    s.require(
        dao.query(InvoiceEntity.class, "e.reservation.id = :p0 and e.status = 'issued'", r.getId())
            .isEmpty(),
        "Resolve and void issued invoices before cancelling");
    s.require(
        dao.query(
                ServiceChargeEntity.class,
                "e.reservation.id = :p0 and e.status = 'active'",
                r.getId())
            .isEmpty(),
        "Void active service charges before cancelling");
    r.setCancelledAt(s.now());
    r.setCancellationReason(reason);
    transition(r, "cancelled", reason);
  }

  public void checkIn(ReservationEntity r) {
    s.require("confirmed".equals(r.getStatus()), "Reservation must be confirmed");
    available(r);
    for (var room : rooms(r.getId())) {
      s.require(room.getRoom() != null, "Assign a physical room before check-in");
      s.require(
          "clean".equals(room.getRoom().getHousekeepingStatus()), "Assigned room must be clean");
      s.require(
          dao.query(
                  ReservationRoomEntity.class,
                  "e.room.id = :p0 and e.status = 'checked_in' and e.reservation.id <> :p1",
                  room.getRoom().getId(),
                  r.getId())
              .isEmpty(),
          "Assigned room still has a checked-in guest");
      LocalDate day = s.today(r.getHotel());
      s.require(covers(room, day), "Today must fall within every room's stay dates");
      s.require(
          dao.query(
                      ReservationCustomerEntity.class,
                      "e.reservationRoom.id = :p0 and e.isPrimaryCustomer = true",
                      room.getId())
                  .size()
              == 1,
          "Each room needs one primary customer");
      room.setActualCheckInAt(s.now());
    }
    transition(r, "checked_in", null);
  }

  public void checkOut(ReservationEntity r) {
    s.require("checked_in".equals(r.getStatus()), "Reservation must be checked in");
    for (var room : rooms(r.getId())) {
      room.setActualCheckOutAt(s.now());
      room.getRoom().setHousekeepingStatus("dirty");
    }
    transition(r, "checked_out", null);
  }

  public void transition(ReservationEntity r, String status, String reason) {
    s.history(r, null, r.getStatus(), status, reason);
    r.setStatus(status);
    for (var room : rooms(r.getId())) {
      s.history(r, room, room.getStatus(), status, reason);
      room.setStatus(status);
    }
  }

  public void assign(ReservationRoomEntity room, RoomEntity physical) {
    s.require(
        Set.of("pending", "confirmed").contains(room.getReservation().getStatus()),
        "Rooms can be assigned before check-in only");
    s.require(
        Set.of("pending", "confirmed").contains(room.getStatus()),
        "Cancelled room reservations cannot be assigned");
    room.setRoom(physical);
    checkAssignedRoom(room);
    if ("confirmed".equals(room.getStatus())) available(room.getReservation());
  }

  public void prepareCustomer(ReservationCustomerEntity e, ReservationCustomerEntity old) {
    if (e.getReservationRoom() != null)
      s.require(
          Set.of("pending", "confirmed").contains(e.getReservationRoom().getStatus()),
          "Cannot add guests to an inactive room reservation");
    s.require(
        e.getReservationRoom() != null && e.getCustomer() != null,
        "Room reservation and customer are required");
    s.require(
        Set.of("pending", "confirmed")
            .contains(e.getReservationRoom().getReservation().getStatus()),
        "Guest list is locked after check-in");
    if (old != null)
      s.same(old.getReservationRoom().getId(), e.getReservationRoom().getId(), "Room reservation");
    var others =
        dao
            .query(
                ReservationCustomerEntity.class,
                "e.reservationRoom.id = :p0",
                e.getReservationRoom().getId())
            .stream()
            .filter(x -> old == null || !x.getId().equals(old.getId()))
            .toList();
    s.require(
        others.stream().noneMatch(x -> x.getCustomer().getId().equals(e.getCustomer().getId())),
        "Customer is already linked to this room");
    s.require(
        others.size() < e.getReservationRoom().getAdults() + e.getReservationRoom().getChildren(),
        "Guest list exceeds booked occupancy");
    if (e.getIsPrimaryCustomer() == null) e.setIsPrimaryCustomer(false);
    s.require(
        !e.getIsPrimaryCustomer()
            || others.stream().noneMatch(x -> Boolean.TRUE.equals(x.getIsPrimaryCustomer())),
        "A primary customer already exists");
  }
}
