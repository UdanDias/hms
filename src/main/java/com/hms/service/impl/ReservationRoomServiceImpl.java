package com.hms.service.impl;

import static com.hms.util.MoneyUtil.money;

import com.hms.dao.AccommodationDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dao.RoomDAO;
import com.hms.dao.RoomRateDAO;
import com.hms.dao.RoomTypeDAO;
import com.hms.dto.ReservationRoomDTO;
import com.hms.entities.AccommodationEntity;
import com.hms.entities.ReservationEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.entities.RoomEntity;
import com.hms.entities.RoomTypeEntity;
import com.hms.entities.enums.RoomStatusValue;
import com.hms.exception.AccommodationNotFoundException;
import com.hms.exception.ReservationNotFoundException;
import com.hms.exception.ReservationRoomNotFoundException;
import com.hms.exception.RoomNotFoundException;
import com.hms.exception.RoomTypeNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.ReservationRoomService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.MoneyUtil;
import com.hms.util.UtilData;
import com.hms.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ReservationRoomServiceImpl implements ReservationRoomService {
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final RoomTypeDAO roomTypeDao;
  private final RoomDAO roomDao;
  private final RoomRateDAO roomRateDao;
  private final AccommodationDAO accommodationDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public ReservationRoomDTO getSelectedReservationRoom(Long reservationRoomId) {
    serviceSecurity.getCurrentUser();
    ReservationRoomEntity reservationRoomEntity =
        reservationRoomDao
            .findById(reservationRoomId)
            .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
    serviceSecurity.checkRead(reservationRoomEntity);
    return entityDTOConvert.convertReservationRoomEntityToReservationRoomDTO(reservationRoomEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationRoomDTO> getAllReservationRooms() {
    serviceSecurity.getCurrentUser();
    List<ReservationRoomEntity> entities =
        reservationRoomDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertReservationRoomEntityListToReservationRoomDTOList(entities);
  }

  @Override
  public ReservationRoomDTO addReservationRoom(ReservationRoomDTO reservationRoomDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationRoomEntity reservationRoomEntity =
        entityDTOConvert.convertReservationRoomDTOToReservationRoomEntity(reservationRoomDTO);
    if (reservationRoomDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(reservationRoomDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      reservationRoomEntity.setReservation(reservation);
    }
    if (reservationRoomDTO.getRoomTypeId() != null) {
      RoomTypeEntity roomType =
          roomTypeDao
              .findById(reservationRoomDTO.getRoomTypeId())
              .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
      reservationRoomEntity.setRoomType(roomType);
    }
    if (reservationRoomDTO.getRoomId() != null) {
      RoomEntity room =
          roomDao
              .findById(reservationRoomDTO.getRoomId())
              .orElseThrow(() -> new RoomNotFoundException("Room not found"));
      reservationRoomEntity.setRoom(room);
    }
    if (reservationRoomDTO.getAccommodationId() != null) {
      AccommodationEntity accommodation =
          accommodationDao
              .findById(reservationRoomDTO.getAccommodationId())
              .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));
      reservationRoomEntity.setAccommodation(accommodation);
    }

    validateReservationRoomForSave(reservationRoomEntity, null);
    validationUtil.validate(reservationRoomEntity);
    reservationRoomDao.save(reservationRoomEntity);
    reservationRoomDao.flush();
    auditRecorder.recordBookingStatus(
        reservationRoomEntity.getReservation(), reservationRoomEntity, null, "pending", null);
    validationUtil.validate(reservationRoomEntity);
    auditRecorder.recordAction("CREATE", "ReservationRoom", reservationRoomEntity.getId());
    reservationRoomDao.flush();
    return entityDTOConvert.convertReservationRoomEntityToReservationRoomDTO(reservationRoomEntity);
  }

  @Override
  public ReservationRoomDTO updateReservationRoom(
      Long reservationRoomId, ReservationRoomDTO reservationRoomDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationRoomEntity reservationRoomEntity =
        reservationRoomDao
            .findById(reservationRoomId)
            .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
    ReservationRoomEntity requestedReservationRoom =
        entityDTOConvert.convertReservationRoomDTOToReservationRoomEntity(reservationRoomDTO);
    if (reservationRoomDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(reservationRoomDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      requestedReservationRoom.setReservation(reservation);
    }
    if (reservationRoomDTO.getRoomTypeId() != null) {
      RoomTypeEntity roomType =
          roomTypeDao
              .findById(reservationRoomDTO.getRoomTypeId())
              .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
      requestedReservationRoom.setRoomType(roomType);
    }
    if (reservationRoomDTO.getRoomId() != null) {
      RoomEntity room =
          roomDao
              .findById(reservationRoomDTO.getRoomId())
              .orElseThrow(() -> new RoomNotFoundException("Room not found"));
      requestedReservationRoom.setRoom(room);
    }
    if (reservationRoomDTO.getAccommodationId() != null) {
      AccommodationEntity accommodation =
          accommodationDao
              .findById(reservationRoomDTO.getAccommodationId())
              .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));
      requestedReservationRoom.setAccommodation(accommodation);
    }

    validateReservationRoomForSave(requestedReservationRoom, reservationRoomEntity);
    validationUtil.validate(requestedReservationRoom);
    reservationRoomEntity.setReservation(requestedReservationRoom.getReservation());
    reservationRoomEntity.setRoomType(requestedReservationRoom.getRoomType());
    reservationRoomEntity.setRoom(requestedReservationRoom.getRoom());
    reservationRoomEntity.setCheckInDate(requestedReservationRoom.getCheckInDate());
    reservationRoomEntity.setCheckOutDate(requestedReservationRoom.getCheckOutDate());
    reservationRoomEntity.setAdults(requestedReservationRoom.getAdults());
    reservationRoomEntity.setChildren(requestedReservationRoom.getChildren());
    reservationRoomEntity.setNightlyRate(requestedReservationRoom.getNightlyRate());
    reservationRoomEntity.setExtraPersonTotal(requestedReservationRoom.getExtraPersonTotal());
    reservationRoomEntity.setDiscountAmount(requestedReservationRoom.getDiscountAmount());
    reservationRoomEntity.setTaxAmount(requestedReservationRoom.getTaxAmount());
    reservationRoomEntity.setServiceChargeAmount(requestedReservationRoom.getServiceChargeAmount());
    reservationRoomEntity.setTotalAmount(requestedReservationRoom.getTotalAmount());
    reservationRoomEntity.setCancellationPolicySnapshot(
        requestedReservationRoom.getCancellationPolicySnapshot());
    reservationRoomEntity.setStatus(requestedReservationRoom.getStatus());
    reservationRoomEntity.setActualCheckInAt(requestedReservationRoom.getActualCheckInAt());
    reservationRoomEntity.setActualCheckOutAt(requestedReservationRoom.getActualCheckOutAt());
    reservationRoomEntity.setAccommodation(requestedReservationRoom.getAccommodation());
    validationUtil.validate(reservationRoomEntity);
    reservationRoomDao.save(reservationRoomEntity);
    auditRecorder.recordAction("UPDATE", "ReservationRoom", reservationRoomEntity.getId());
    reservationRoomDao.flush();
    return entityDTOConvert.convertReservationRoomEntityToReservationRoomDTO(reservationRoomEntity);
  }

  private void validateReservationRoomForSave(
      ReservationRoomEntity reservationRoomEntity, ReservationRoomEntity existingReservationRoom) {
    validationUtil.require(
        reservationRoomEntity.getReservation() != null
            && reservationRoomEntity.getRoomType() != null
            && reservationRoomEntity.getAccommodation() != null,
        "Reservation, room type and accommodation are required");
    requirePendingReservation(reservationRoomEntity.getReservation());
    if (existingReservationRoom != null) {
      validationUtil.require(
          "pending".equals(existingReservationRoom.getStatus()),
          "Cancelled room reservations cannot be edited");
      requirePendingReservation(existingReservationRoom.getReservation());
      validationUtil.same(
          existingReservationRoom.getReservation().getId(),
          reservationRoomEntity.getReservation().getId(),
          "Reservation");
    }
    var hotel = reservationRoomEntity.getReservation().getHotel();
    validationUtil.same(
        hotel.getId(), reservationRoomEntity.getRoomType().getHotel().getId(), "Room type");
    validationUtil.same(
        hotel.getId(),
        reservationRoomEntity.getAccommodation().getHotel().getId(),
        "Accommodation");
    validationUtil.require(
        Boolean.TRUE.equals(reservationRoomEntity.getRoomType().getIsActive()),
        "Room type is inactive");
    validationUtil.require(
        reservationRoomEntity.getCheckInDate() != null
            && reservationRoomEntity.getCheckOutDate() != null
            && reservationRoomEntity
                .getCheckOutDate()
                .isAfter(reservationRoomEntity.getCheckInDate()),
        "Checkout must be after check-in");
    validationUtil.require(
        !reservationRoomEntity.getCheckInDate().isBefore(UtilData.todayAtHotel(hotel)),
        "Cannot create a stay in the past");
    validationUtil.require(
        ChronoUnit.DAYS.between(
                reservationRoomEntity.getCheckInDate(), reservationRoomEntity.getCheckOutDate())
            <= 366,
        "Maximum stay is 366 nights");
    int a = reservationRoomEntity.getAdults() == null ? 0 : reservationRoomEntity.getAdults(),
        c = reservationRoomEntity.getChildren() == null ? 0 : reservationRoomEntity.getChildren();
    validationUtil.require(
        a >= 1
            && c >= 0
            && a <= reservationRoomEntity.getRoomType().getMaxAdults()
            && c <= reservationRoomEntity.getRoomType().getMaxChildren()
            && a + c <= reservationRoomEntity.getRoomType().getMaxOccupancy(),
        "Occupancy exceeds room-type limits");
    reservationRoomEntity.setAdults(a);
    reservationRoomEntity.setChildren(c);
    validateAssignedRoom(reservationRoomEntity);
    reservationRoomEntity.setNightlyRate(
        MoneyUtil.nonnegative(
            reservationRoomEntity.getNightlyRate() == null
                ? reservationRoomEntity.getAccommodation().getPrice()
                : reservationRoomEntity.getNightlyRate()));
    reservationRoomEntity.setExtraPersonTotal(
        MoneyUtil.nonnegative(reservationRoomEntity.getExtraPersonTotal()));
    reservationRoomEntity.setDiscountAmount(
        MoneyUtil.nonnegative(reservationRoomEntity.getDiscountAmount()));
    reservationRoomEntity.setTaxAmount(MoneyUtil.nonnegative(reservationRoomEntity.getTaxAmount()));
    reservationRoomEntity.setServiceChargeAmount(
        MoneyUtil.nonnegative(reservationRoomEntity.getServiceChargeAmount()));
    BigDecimal base =
        reservationRoomEntity
            .getNightlyRate()
            .multiply(
                BigDecimal.valueOf(
                    ChronoUnit.DAYS.between(
                        reservationRoomEntity.getCheckInDate(),
                        reservationRoomEntity.getCheckOutDate())))
            .add(reservationRoomEntity.getExtraPersonTotal());
    validationUtil.require(
        reservationRoomEntity.getDiscountAmount().compareTo(base) <= 0,
        "Discount exceeds room charge");
    reservationRoomEntity.setTotalAmount(
        money(
            base.subtract(reservationRoomEntity.getDiscountAmount())
                .add(reservationRoomEntity.getTaxAmount())
                .add(reservationRoomEntity.getServiceChargeAmount())));
    reservationRoomEntity.setStatus("pending");
    reservationRoomEntity.setActualCheckInAt(null);
    reservationRoomEntity.setActualCheckOutAt(null);
    if (reservationRoomEntity.getCancellationPolicySnapshot() == null)
      reservationRoomEntity.setCancellationPolicySnapshot("");
    validateStayRestrictions(reservationRoomEntity);
  }

  @Override
  public ReservationRoomDTO cancelReservationRoom(Long reservationRoomId, String reason) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationRoomEntity reservationRoomEntity =
        reservationRoomDao
            .findById(reservationRoomId)
            .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
    requirePendingReservation(reservationRoomEntity.getReservation());
    validationUtil.require(
        "pending".equals(reservationRoomEntity.getStatus()),
        "Only pending room reservations can be cancelled individually");
    validationUtil.text(reason, "Cancellation reason");
    auditRecorder.recordBookingStatus(
        reservationRoomEntity.getReservation(),
        reservationRoomEntity,
        reservationRoomEntity.getStatus(),
        "cancelled",
        reason);
    reservationRoomEntity.setStatus("cancelled");
    validationUtil.validate(reservationRoomEntity);
    auditRecorder.recordAction("CANCEL", "ReservationRoom", reservationRoomEntity.getId());
    reservationRoomDao.flush();
    return entityDTOConvert.convertReservationRoomEntityToReservationRoomDTO(reservationRoomEntity);
  }

  @Override
  public ReservationRoomDTO assignRoom(Long reservationRoomId, Long roomId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationRoomEntity reservationRoomEntity =
        reservationRoomDao
            .findById(reservationRoomId)
            .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
    assignPhysicalRoom(
        reservationRoomEntity,
        roomDao.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found")));
    validationUtil.validate(reservationRoomEntity);
    auditRecorder.recordAction("ASSIGN_ROOM", "ReservationRoom", reservationRoomEntity.getId());
    reservationRoomDao.flush();
    return entityDTOConvert.convertReservationRoomEntityToReservationRoomDTO(reservationRoomEntity);
  }

  private void requirePendingReservation(ReservationEntity r) {
    validationUtil.require(
        "pending".equals(r.getStatus()), "Only pending reservations can be edited");
  }

  private void validateAssignedRoom(ReservationRoomEntity e) {
    if (e.getRoom() == null) return;
    var r = e.getRoom();
    validationUtil.same(r.getHotel().getId(), e.getReservation().getHotel().getId(), "Room");
    validationUtil.same(r.getRoomType().getId(), e.getRoomType().getId(), "Room type");
    validationUtil.require(isRoomSellable(r), "Assigned room is unavailable or servicing");
  }

  private void validateStayRestrictions(ReservationRoomEntity e) {
    long nights = ChronoUnit.DAYS.between(e.getCheckInDate(), e.getCheckOutDate());
    for (var rate :
        roomRateDao.findByRoomTypeIdAndDateRange(
            e.getRoomType().getId(), e.getCheckInDate(), e.getCheckOutDate())) {
      validationUtil.require(
          !Boolean.TRUE.equals(rate.getStopSell()),
          "Room type is closed for sale on " + rate.getStayDate());
      validationUtil.require(
          nights >= rate.getMinimumNights(), "Minimum stay requirement is not met");
    }
  }

  private void assignPhysicalRoom(ReservationRoomEntity room, RoomEntity physical) {
    validationUtil.require(
        Set.of("pending", "confirmed").contains(room.getReservation().getStatus()),
        "Rooms can be assigned before check-in only");
    validationUtil.require(
        Set.of("pending", "confirmed").contains(room.getStatus()),
        "Cancelled room reservations cannot be assigned");
    room.setRoom(physical);
    validateAssignedRoom(room);
    if ("confirmed".equals(room.getStatus()))
      validateReservationAvailability(room.getReservation());
  }

  private boolean isRoomSellable(RoomEntity r) {
    return "active".equals(r.getOperationalStatus())
        && r.getRoomStatus().getStatus() == RoomStatusValue.AVAILABLE;
  }

  private void validateReservationAvailability(ReservationEntity reservation) {
    if ("pending".equals(reservation.getStatus()))
      validationUtil.require(
          Boolean.TRUE.equals(reservation.getHotel().getIsActive()), "Hotel is inactive");
    var requested = findReservationRooms(reservation.getId());
    validationUtil.require(!requested.isEmpty(), "Add at least one room before confirmation");
    for (var wanted : requested) {
      validateAssignedRoom(wanted);
      if ("pending".equals(reservation.getStatus()))
        validationUtil.require(
            Boolean.TRUE.equals(wanted.getRoomType().getIsActive()), "Room type is inactive");
      if ("pending".equals(reservation.getStatus())) validateStayRestrictions(wanted);
      long capacity =
          roomDao.findByRoomTypeId(wanted.getRoomType().getId()).stream()
              .filter(this::isRoomSellable)
              .count();
      var occupied =
          reservationRoomDao.findBlockingStaysExcludingReservation(
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
                            && coversDate(x, d))
                .count();
        long taken = occupied.stream().filter(x -> coversDate(x, d)).count();
        validationUtil.require(need + taken <= capacity, "No room-type capacity on " + day);
      }
      if (wanted.getRoom() != null) {
        validationUtil.require(
            occupied.stream()
                .noneMatch(
                    x ->
                        x.getRoom() != null
                            && x.getRoom().getId().equals(wanted.getRoom().getId())),
            "Assigned room overlaps another booking");
        validationUtil.require(
            requested.stream()
                .noneMatch(
                    x ->
                        !x.getId().equals(wanted.getId())
                            && x.getRoom() != null
                            && x.getRoom().getId().equals(wanted.getRoom().getId())
                            && datesOverlap(x, wanted)),
            "The same room is assigned twice for overlapping dates");
      }
    }
  }

  private List<ReservationRoomEntity> findReservationRooms(Long id) {
    return reservationRoomDao.findNonCancelledByReservationId(id);
  }

  private boolean coversDate(ReservationRoomEntity r, LocalDate day) {
    return !day.isBefore(r.getCheckInDate()) && day.isBefore(r.getCheckOutDate());
  }

  private boolean datesOverlap(ReservationRoomEntity a, ReservationRoomEntity b) {
    return a.getCheckInDate().isBefore(b.getCheckOutDate())
        && b.getCheckInDate().isBefore(a.getCheckOutDate());
  }
}
