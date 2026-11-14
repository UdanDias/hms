package com.hms.service.impl;

import static com.hms.util.MoneyUtil.money;

import com.hms.dao.CustomerDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.InvoiceDAO;
import com.hms.dao.InvoiceItemDAO;
import com.hms.dao.PaymentDAO;
import com.hms.dao.RefundDAO;
import com.hms.dao.ReservationCustomerDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dao.RoomDAO;
import com.hms.dao.RoomRateDAO;
import com.hms.dao.ServiceChargeDAO;
import com.hms.dto.ReservationDTO;
import com.hms.entities.CustomerEntity;
import com.hms.entities.HotelEntity;
import com.hms.entities.InvoiceEntity;
import com.hms.entities.PaymentEntity;
import com.hms.entities.RefundEntity;
import com.hms.entities.ReservationEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.entities.RoomEntity;
import com.hms.entities.enums.RoomStatusValue;
import com.hms.exception.CustomerNotFoundException;
import com.hms.exception.HotelNotFoundException;
import com.hms.exception.ReservationNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.ReservationService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
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
public class ReservationServiceImpl implements ReservationService {
  private final CustomerDAO customerDao;
  private final PaymentDAO paymentDao;
  private final RefundDAO refundDao;
  private final InvoiceDAO invoiceDao;
  private final InvoiceItemDAO invoiceItemDao;
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final RoomDAO roomDao;
  private final RoomRateDAO roomRateDao;
  private final ServiceChargeDAO serviceChargeDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final ReservationCustomerDAO reservationCustomerDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public ReservationDTO getSelectedReservation(Long reservationId) {
    serviceSecurity.getCurrentUser();
    ReservationEntity reservationEntity =
        reservationDao
            .findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    serviceSecurity.checkRead(reservationEntity);
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationDTO> getAllReservations() {
    serviceSecurity.getCurrentUser();
    List<ReservationEntity> entities =
        reservationDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertReservationEntityListToReservationDTOList(entities);
  }

  @Override
  public ReservationDTO addReservation(ReservationDTO reservationDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationEntity reservationEntity =
        entityDTOConvert.convertReservationDTOToReservationEntity(reservationDTO);
    if (reservationDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(reservationDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      reservationEntity.setHotel(hotel);
    }
    if (reservationDTO.getBookerCustomerId() != null) {
      CustomerEntity bookerCustomer =
          customerDao
              .findById(reservationDTO.getBookerCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      reservationEntity.setBookerCustomer(bookerCustomer);
    }

    validateReservationForSave(reservationEntity, null);
    validationUtil.validate(reservationEntity);
    reservationDao.save(reservationEntity);
    reservationDao.flush();
    auditRecorder.recordBookingStatus(reservationEntity, null, null, "pending", null);
    validationUtil.validate(reservationEntity);
    auditRecorder.recordAction("CREATE", "Reservation", reservationEntity.getId());
    reservationDao.flush();
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  @Override
  public ReservationDTO updateReservation(Long reservationId, ReservationDTO reservationDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationEntity reservationEntity =
        reservationDao
            .findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    ReservationEntity requestedReservation =
        entityDTOConvert.convertReservationDTOToReservationEntity(reservationDTO);
    if (reservationDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(reservationDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      requestedReservation.setHotel(hotel);
    }
    if (reservationDTO.getBookerCustomerId() != null) {
      CustomerEntity bookerCustomer =
          customerDao
              .findById(reservationDTO.getBookerCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      requestedReservation.setBookerCustomer(bookerCustomer);
    }

    validateReservationForSave(requestedReservation, reservationEntity);
    validationUtil.validate(requestedReservation);
    reservationEntity.setHotel(requestedReservation.getHotel());
    reservationEntity.setBookingReference(requestedReservation.getBookingReference());
    reservationEntity.setBookerCustomer(requestedReservation.getBookerCustomer());
    reservationEntity.setStatus(requestedReservation.getStatus());
    reservationEntity.setBookingSource(requestedReservation.getBookingSource());
    reservationEntity.setCurrencyCode(requestedReservation.getCurrencyCode());
    reservationEntity.setSpecialRequests(requestedReservation.getSpecialRequests());
    reservationEntity.setCreatedBy(requestedReservation.getCreatedBy());
    reservationEntity.setConfirmedAt(requestedReservation.getConfirmedAt());
    reservationEntity.setCancelledAt(requestedReservation.getCancelledAt());
    reservationEntity.setCancellationReason(requestedReservation.getCancellationReason());
    validationUtil.validate(reservationEntity);
    reservationDao.save(reservationEntity);
    auditRecorder.recordAction("UPDATE", "Reservation", reservationEntity.getId());
    reservationDao.flush();
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  private void validateReservationForSave(
      ReservationEntity reservationEntity, ReservationEntity existingReservation) {
    if (reservationEntity.getSpecialRequests() == null) reservationEntity.setSpecialRequests("");
    validationUtil.require(
        reservationEntity.getHotel() != null
            && Boolean.TRUE.equals(reservationEntity.getHotel().getIsActive()),
        "An active hotel is required");
    validationUtil.require(
        reservationEntity.getBookerCustomer() != null, "Booker customer is required");
    if (existingReservation == null) {
      reservationEntity.setStatus("pending");
      reservationEntity.setBookingReference(UtilData.generateNumber("RES"));
      reservationEntity.setCreatedBy(serviceSecurity.getCurrentUser());
      reservationEntity.setConfirmedAt(null);
      reservationEntity.setCancelledAt(null);
      reservationEntity.setCancellationReason(null);
    } else {
      requirePendingReservation(existingReservation);
      validationUtil.same(
          reservationEntity.getHotel().getId(), existingReservation.getHotel().getId(), "Hotel");
      reservationEntity.setStatus(existingReservation.getStatus());
      reservationEntity.setBookingReference(existingReservation.getBookingReference());
      reservationEntity.setCreatedBy(existingReservation.getCreatedBy());
      reservationEntity.setConfirmedAt(existingReservation.getConfirmedAt());
      reservationEntity.setCancelledAt(existingReservation.getCancelledAt());
      reservationEntity.setCancellationReason(existingReservation.getCancellationReason());
    }
    reservationEntity.setCurrencyCode(reservationEntity.getHotel().getCurrencyCode());
    validationUtil.text(reservationEntity.getBookingSource(), "Booking source");
  }

  @Override
  public ReservationDTO confirmReservation(Long reservationId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationEntity reservationEntity =
        reservationDao
            .findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    confirmBooking(reservationEntity);
    validationUtil.validate(reservationEntity);
    auditRecorder.recordAction("CONFIRM", "Reservation", reservationEntity.getId());
    reservationDao.flush();
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  @Override
  public ReservationDTO cancelReservation(Long reservationId, String reason) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationEntity reservationEntity =
        reservationDao
            .findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    cancelBooking(reservationEntity, reason);
    validationUtil.validate(reservationEntity);
    auditRecorder.recordAction("CANCEL", "Reservation", reservationEntity.getId());
    reservationDao.flush();
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  @Override
  public ReservationDTO checkInReservation(Long reservationId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationEntity reservationEntity =
        reservationDao
            .findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    checkInBooking(reservationEntity);
    validationUtil.validate(reservationEntity);
    auditRecorder.recordAction("CHECK_IN", "Reservation", reservationEntity.getId());
    reservationDao.flush();
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  @Override
  public ReservationDTO checkOutReservation(Long reservationId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationEntity reservationEntity =
        reservationDao
            .findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    requireSettledReservation(reservationEntity);
    checkOutBooking(reservationEntity);
    validationUtil.validate(reservationEntity);
    auditRecorder.recordAction("CHECK_OUT", "Reservation", reservationEntity.getId());
    reservationDao.flush();
    return entityDTOConvert.convertReservationEntityToReservationDTO(reservationEntity);
  }

  private void requirePendingReservation(ReservationEntity r) {
    validationUtil.require(
        "pending".equals(r.getStatus()), "Only pending reservations can be edited");
  }

  private void confirmBooking(ReservationEntity r) {
    requirePendingReservation(r);
    validateReservationAvailability(r);
    r.setConfirmedAt(UtilData.generateNow());
    changeReservationStatus(r, "confirmed", null);
  }

  private void cancelBooking(ReservationEntity r, String reason) {
    validationUtil.require(
        Set.of("pending", "confirmed").contains(r.getStatus()),
        "Only pending or confirmed reservations can be cancelled");
    validationUtil.text(reason, "Cancellation reason");
    validationUtil.require(
        invoiceDao.findIssuedByReservationId(r.getId()).isEmpty(),
        "Resolve and void issued invoices before cancelling");
    validationUtil.require(
        serviceChargeDao.findActiveByReservationId(r.getId()).isEmpty(),
        "Void active service charges before cancelling");
    r.setCancelledAt(UtilData.generateNow());
    r.setCancellationReason(reason);
    changeReservationStatus(r, "cancelled", reason);
  }

  private void checkInBooking(ReservationEntity r) {
    validationUtil.require("confirmed".equals(r.getStatus()), "Reservation must be confirmed");
    validateReservationAvailability(r);
    for (var room : findReservationRooms(r.getId())) {
      validationUtil.require(room.getRoom() != null, "Assign a physical room before check-in");
      validationUtil.require(
          "clean".equals(room.getRoom().getHousekeepingStatus()), "Assigned room must be clean");
      validationUtil.require(
          reservationRoomDao
              .findCheckedInOccupantsExcludingReservation(room.getRoom().getId(), r.getId())
              .isEmpty(),
          "Assigned room still has a checked-in guest");
      LocalDate day = UtilData.todayAtHotel(r.getHotel());
      validationUtil.require(
          coversDate(room, day), "Today must fall within every room's stay dates");
      validationUtil.require(
          reservationCustomerDao.findPrimaryGuestsByReservationRoomId(room.getId()).size() == 1,
          "Each room needs one primary customer");
      room.setActualCheckInAt(UtilData.generateNow());
    }
    changeReservationStatus(r, "checked_in", null);
  }

  private void checkOutBooking(ReservationEntity r) {
    validationUtil.require("checked_in".equals(r.getStatus()), "Reservation must be checked in");
    for (var room : findReservationRooms(r.getId())) {
      room.setActualCheckOutAt(UtilData.generateNow());
      room.getRoom().setHousekeepingStatus("dirty");
    }
    changeReservationStatus(r, "checked_out", null);
  }

  private void requireSettledReservation(ReservationEntity r) {
    var invoices = invoiceDao.findNonVoidByReservationId(r.getId());
    validationUtil.require(!invoices.isEmpty(), "An issued invoice is required before checkout");
    for (var i : invoices)
      validationUtil.require(
          "issued".equals(i.getStatus()) && calculateOutstandingBalance(i).signum() == 0,
          "All invoices must be issued and settled before checkout");
    for (var room : reservationRoomDao.findNonCancelledByReservationId(r.getId()))
      validationUtil.require(
          !invoiceItemDao.findIssuedByReservationRoomId(room.getId()).isEmpty(),
          "Every room must be invoiced before checkout");
    for (var c : serviceChargeDao.findActiveByReservationId(r.getId()))
      validationUtil.require(
          !invoiceItemDao.findIssuedByServiceChargeId(c.getId()).isEmpty(),
          "Every active service charge must be invoiced before checkout");
    validationUtil.require(
        paymentDao.findUnallocatedByReservationId(r.getId()).stream()
            .allMatch(x -> calculateNetPayment(x).signum() == 0),
        "Allocate or refund unapplied deposits before checkout");
  }

  private List<ReservationRoomEntity> findReservationRooms(Long id) {
    return reservationRoomDao.findNonCancelledByReservationId(id);
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

  private boolean coversDate(ReservationRoomEntity r, LocalDate day) {
    return !day.isBefore(r.getCheckInDate()) && day.isBefore(r.getCheckOutDate());
  }

  private void changeReservationStatus(ReservationEntity r, String status, String reason) {
    auditRecorder.recordBookingStatus(r, null, r.getStatus(), status, reason);
    r.setStatus(status);
    for (var room : findReservationRooms(r.getId())) {
      auditRecorder.recordBookingStatus(r, room, room.getStatus(), status, reason);
      room.setStatus(status);
    }
  }

  private BigDecimal calculateNetPayment(PaymentEntity p) {
    return p.getAmount().subtract(calculateRefundedAmount(p));
  }

  private BigDecimal calculateOutstandingBalance(InvoiceEntity i) {
    return money(i.getGrandTotal().subtract(calculatePaidAmount(i)));
  }

  private void validateAssignedRoom(ReservationRoomEntity e) {
    if (e.getRoom() == null) return;
    var r = e.getRoom();
    validationUtil.same(r.getHotel().getId(), e.getReservation().getHotel().getId(), "Room");
    validationUtil.same(r.getRoomType().getId(), e.getRoomType().getId(), "Room type");
    validationUtil.require(isRoomSellable(r), "Assigned room is unavailable or servicing");
  }

  private boolean isRoomSellable(RoomEntity r) {
    return "active".equals(r.getOperationalStatus())
        && r.getRoomStatus().getStatus() == RoomStatusValue.AVAILABLE;
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

  private boolean datesOverlap(ReservationRoomEntity a, ReservationRoomEntity b) {
    return a.getCheckInDate().isBefore(b.getCheckOutDate())
        && b.getCheckInDate().isBefore(a.getCheckOutDate());
  }

  private BigDecimal calculateRefundedAmount(PaymentEntity p) {
    return refundDao.findCompletedByPaymentId(p.getId()).stream()
        .map(RefundEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculatePaidAmount(InvoiceEntity i) {
    return paymentDao.findCompletedByInvoiceId(i.getId()).stream()
        .map(this::calculateNetPayment)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
