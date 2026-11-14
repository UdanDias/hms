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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomStatusServiceImpl implements RoomStatusService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public RoomStatusDTO getSelectedRoomStatus(Long roomStatusId) {
    s.actor();
    var e = dao.require(RoomStatusEntity.class, roomStatusId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<RoomStatusDTO> getAllRoomStatuses() {
    s.actor();
    return dao.all(RoomStatusEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoomStatusDTO addRoomStatus(RoomStatusDTO dto) {
    s.admin();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  private void prepare(RoomStatusEntity e, RoomStatusEntity old) {
    s.require(e.getStatus() != null, "Room status is required");
  }

  private RoomStatusDTO finish(RoomStatusEntity e, String action) {
    s.validate(e);
    s.audit(action, "RoomStatus", e.getStatusId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
