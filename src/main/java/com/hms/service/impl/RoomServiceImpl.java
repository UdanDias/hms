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
public class RoomServiceImpl implements RoomService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public RoomDTO getSelectedRoom(Long roomId) {
    s.actor();
    var e = dao.require(RoomEntity.class, roomId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<RoomDTO> getAllRooms() {
    s.actor();
    return dao.all(RoomEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoomDTO addRoom(RoomDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoomDTO updateRoom(Long id, RoomDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var old = dao.require(RoomEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(RoomEntity e, RoomEntity old) {
    if (e.getNotes() == null) e.setNotes("");
    s.require(
        e.getHotel() != null && e.getRoomType() != null && e.getRoomStatus() != null,
        "Hotel, room type and room status are required");
    s.same(e.getHotel().getId(), e.getRoomType().getHotel().getId(), "Room type");
    s.text(e.getRoomNumber(), "Room number");
    if (e.getOperationalStatus() == null) e.setOperationalStatus("active");
    if (e.getHousekeepingStatus() == null) e.setHousekeepingStatus("clean");
    s.require(
        Set.of("active", "out_of_service", "retired").contains(e.getOperationalStatus()),
        "Invalid operational status");
    s.require(
        Set.of("clean", "dirty", "inspecting").contains(e.getHousekeepingStatus()),
        "Invalid housekeeping status");
    if (old != null) {
      s.same(old.getHotel().getId(), e.getHotel().getId(), "Hotel");
      boolean inventoryChange =
          !old.getRoomType().getId().equals(e.getRoomType().getId())
              || !old.getOperationalStatus().equals(e.getOperationalStatus())
              || !old.getRoomStatus().getStatusId().equals(e.getRoomStatus().getStatusId());
      if (inventoryChange)
        s.require(
            dao.query(
                    ReservationRoomEntity.class,
                    "e.roomType.id = :p0 and e.status in ('confirmed','checked_in') and"
                        + " e.checkOutDate > :p1",
                    old.getRoomType().getId(),
                    s.today(e.getHotel()))
                .isEmpty(),
            "Resolve active bookings for this room type before changing its inventory");
    }
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoomDTO updateHousekeepingStatus(Long roomId, String status) {
    s.allow("ADMIN", "MANAGER", "RECEPTIONIST", "HOUSEKEEPING");
    dao.lockHotels();
    s.require(
        Set.of("clean", "dirty", "inspecting").contains(status), "Invalid housekeeping status");
    var e = dao.require(RoomEntity.class, roomId);
    e.setHousekeepingStatus(status);
    return finish(e, "HOUSEKEEPING");
  }

  private RoomDTO finish(RoomEntity e, String action) {
    s.validate(e);
    s.audit(action, "Room", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
