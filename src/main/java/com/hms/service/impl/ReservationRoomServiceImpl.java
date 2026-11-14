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
public class ReservationRoomServiceImpl implements ReservationRoomService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BookingRules booking;

  @Override
  public ReservationRoomDTO getSelectedReservationRoom(Long reservationRoomId) {
    s.actor();
    var e = dao.require(ReservationRoomEntity.class, reservationRoomId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<ReservationRoomDTO> getAllReservationRooms() {
    s.actor();
    return dao.all(ReservationRoomEntity.class).stream()
        .filter(s::visible)
        .map(mapper::toDTO)
        .toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationRoomDTO addReservationRoom(ReservationRoomDTO dto) {
    s.write();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    s.history(e.getReservation(), e, null, "pending", null);
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationRoomDTO updateReservationRoom(Long id, ReservationRoomDTO dto) {
    s.write();
    var old = dao.require(ReservationRoomEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(ReservationRoomEntity e, ReservationRoomEntity old) {
    booking.prepareRoom(e, old);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationRoomDTO cancelReservationRoom(Long reservationRoomId, String reason) {
    s.write();
    var e = dao.require(ReservationRoomEntity.class, reservationRoomId);
    booking.draft(e.getReservation());
    s.require(
        "pending".equals(e.getStatus()),
        "Only pending room reservations can be cancelled individually");
    s.text(reason, "Cancellation reason");
    s.history(e.getReservation(), e, e.getStatus(), "cancelled", reason);
    e.setStatus("cancelled");
    return finish(e, "CANCEL");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReservationRoomDTO assignRoom(Long reservationRoomId, Long roomId) {
    s.write();
    var e = dao.require(ReservationRoomEntity.class, reservationRoomId);
    booking.assign(e, dao.require(RoomEntity.class, roomId));
    return finish(e, "ASSIGN_ROOM");
  }

  private ReservationRoomDTO finish(ReservationRoomEntity e, String action) {
    s.validate(e);
    s.audit(action, "ReservationRoom", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
