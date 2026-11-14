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
public class ReservationCustomerServiceImpl implements ReservationCustomerService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BookingRules booking;

  @Override
  public ReservationCustomerDTO getSelectedReservationCustomer(Long reservationCustomerId) {
    s.actor();
    var e = dao.require(ReservationCustomerEntity.class, reservationCustomerId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<ReservationCustomerDTO> getAllReservationCustomers() {
    s.actor();
    return dao.all(ReservationCustomerEntity.class).stream()
        .filter(s::visible)
        .map(mapper::toDTO)
        .toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationCustomerDTO addReservationCustomer(ReservationCustomerDTO dto) {
    s.write();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationCustomerDTO updateReservationCustomer(Long id, ReservationCustomerDTO dto) {
    s.write();
    var old = dao.require(ReservationCustomerEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteReservationCustomer(Long id) {
    s.write();
    var e = dao.require(ReservationCustomerEntity.class, id);
    s.require(
        Set.of("pending", "confirmed")
            .contains(e.getReservationRoom().getReservation().getStatus()),
        "Guest list is locked after check-in");
    s.audit("DELETE", "ReservationCustomer", id);
    dao.remove(e);
    dao.flush();
  }

  private void prepare(ReservationCustomerEntity e, ReservationCustomerEntity old) {
    booking.prepareCustomer(e, old);
  }

  private ReservationCustomerDTO finish(ReservationCustomerEntity e, String action) {
    s.validate(e);
    s.audit(action, "ReservationCustomer", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
