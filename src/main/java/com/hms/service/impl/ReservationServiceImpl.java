package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BookingRules booking;
  private final BillingRules billing;

  @Override
  public ReservationDTO getSelectedReservation(Long reservationId) {
    s.actor();
    var e = dao.require(ReservationEntity.class, reservationId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<ReservationDTO> getAllReservations() {
    s.actor();
    return dao.all(ReservationEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationDTO addReservation(ReservationDTO dto) {
    s.write();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    s.history(e, null, null, "pending", null);
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationDTO updateReservation(Long id, ReservationDTO dto) {
    s.write();
    var old = dao.require(ReservationEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(ReservationEntity e, ReservationEntity old) {
    booking.prepare(e, old);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationDTO confirmReservation(Long reservationId) {
    s.write();
    var e = dao.require(ReservationEntity.class, reservationId);
    booking.confirm(e);
    return finish(e, "CONFIRM");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationDTO cancelReservation(Long reservationId, String reason) {
    s.write();
    var e = dao.require(ReservationEntity.class, reservationId);
    booking.cancel(e, reason);
    return finish(e, "CANCEL");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationDTO checkInReservation(Long reservationId) {
    s.write();
    var e = dao.require(ReservationEntity.class, reservationId);
    booking.checkIn(e);
    return finish(e, "CHECK_IN");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationDTO checkOutReservation(Long reservationId) {
    s.write();
    var e = dao.require(ReservationEntity.class, reservationId);
    billing.settledForCheckout(e);
    booking.checkOut(e);
    return finish(e, "CHECK_OUT");
  }

  private ReservationDTO finish(ReservationEntity e, String action) {
    s.validate(e);
    s.audit(action, "Reservation", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
