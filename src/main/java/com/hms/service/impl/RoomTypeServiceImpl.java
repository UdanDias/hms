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
public class RoomTypeServiceImpl implements RoomTypeService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public RoomTypeDTO getSelectedRoomType(Long roomTypeId) {
    s.actor();
    var e = dao.require(RoomTypeEntity.class, roomTypeId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<RoomTypeDTO> getAllRoomTypes() {
    s.actor();
    return dao.all(RoomTypeEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoomTypeDTO addRoomType(RoomTypeDTO dto) {
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
  public RoomTypeDTO updateRoomType(Long id, RoomTypeDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var old = dao.require(RoomTypeEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(RoomTypeEntity e, RoomTypeEntity old) {
    if (e.getDescription() == null) e.setDescription("");
    if (e.getBedDescription() == null) e.setBedDescription("");
    s.require(e.getHotel() != null, "Hotel is required");
    s.text(e.getName(), "Room type name");
    s.require(
        e.getMaxAdults() != null
            && e.getMaxAdults() >= 1
            && e.getMaxChildren() != null
            && e.getMaxChildren() >= 0
            && e.getMaxOccupancy() != null
            && e.getMaxOccupancy() >= 1
            && e.getMaxAdults() <= e.getMaxOccupancy(),
        "Invalid occupancy limits");
    if (e.getIsActive() == null) e.setIsActive(true);
    if (old != null) {
      s.same(old.getHotel().getId(), e.getHotel().getId(), "Hotel");
      for (var stay :
          dao.query(
              ReservationRoomEntity.class,
              "e.roomType.id = :p0 and e.status in ('confirmed','checked_in')",
              old.getId()))
        s.require(
            stay.getAdults() <= e.getMaxAdults()
                && stay.getChildren() <= e.getMaxChildren()
                && stay.getAdults() + stay.getChildren() <= e.getMaxOccupancy(),
            "New occupancy limits conflict with active bookings");
    }
  }

  private RoomTypeDTO finish(RoomTypeEntity e, String action) {
    s.validate(e);
    s.audit(action, "RoomType", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
