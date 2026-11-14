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
public class RoomRateServiceImpl implements RoomRateService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public RoomRateDTO getSelectedRoomRate(Long roomRateId) {
    s.actor();
    var e = dao.require(RoomRateEntity.class, roomRateId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<RoomRateDTO> getAllRoomRates() {
    s.actor();
    return dao.all(RoomRateEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoomRateDTO addRoomRate(RoomRateDTO dto) {
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
  public RoomRateDTO updateRoomRate(Long id, RoomRateDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var old = dao.require(RoomRateEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteRoomRate(Long id) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var e = dao.require(RoomRateEntity.class, id);
    s.audit("DELETE", "RoomRate", id);
    dao.remove(e);
    dao.flush();
  }

  private void prepare(RoomRateEntity e, RoomRateEntity old) {
    s.require(
        e.getRoomType() != null && e.getStayDate() != null, "Room type and stay date are required");
    e.setNightlyRate(s.nonnegative(e.getNightlyRate()));
    e.setExtraAdultRate(s.nonnegative(e.getExtraAdultRate()));
    e.setExtraChildRate(s.nonnegative(e.getExtraChildRate()));
    if (e.getMinimumNights() == null) e.setMinimumNights(1);
    s.require(e.getMinimumNights() >= 1, "Minimum nights must be positive");
    if (e.getStopSell() == null) e.setStopSell(false);
    if (old != null) s.same(old.getRoomType().getId(), e.getRoomType().getId(), "Room type");
  }

  private RoomRateDTO finish(RoomRateEntity e, String action) {
    s.validate(e);
    s.audit(action, "RoomRate", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
