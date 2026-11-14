package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.RoomStatusDAO;
import com.hms.dto.RoomStatusDTO;
import com.hms.entities.RoomStatusEntity;
import com.hms.exception.RoomStatusNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.RoomStatusService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.ValidationUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class RoomStatusServiceImpl implements RoomStatusService {
  private final HotelDAO hotelDao;
  private final RoomStatusDAO roomStatusDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public RoomStatusDTO getSelectedRoomStatus(Long roomStatusId) {
    serviceSecurity.getCurrentUser();
    RoomStatusEntity roomStatusEntity =
        roomStatusDao
            .findById(roomStatusId)
            .orElseThrow(() -> new RoomStatusNotFoundException("RoomStatus not found"));
    serviceSecurity.checkRead(roomStatusEntity);
    return entityDTOConvert.convertRoomStatusEntityToRoomStatusDTO(roomStatusEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomStatusDTO> getAllRoomStatuses() {
    serviceSecurity.getCurrentUser();
    List<RoomStatusEntity> entities =
        roomStatusDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertRoomStatusEntityListToRoomStatusDTOList(entities);
  }

  @Override
  public RoomStatusDTO addRoomStatus(RoomStatusDTO roomStatusDTO) {
    serviceSecurity.requireRoles("ADMIN");
    hotelDao.lockAllForWrite();
    RoomStatusEntity roomStatusEntity =
        entityDTOConvert.convertRoomStatusDTOToRoomStatusEntity(roomStatusDTO);

    validateRoomStatusForSave(roomStatusEntity, null);
    validationUtil.validate(roomStatusEntity);
    roomStatusDao.save(roomStatusEntity);
    auditRecorder.recordAction("CREATE", "RoomStatus", roomStatusEntity.getStatusId());
    roomStatusDao.flush();
    return entityDTOConvert.convertRoomStatusEntityToRoomStatusDTO(roomStatusEntity);
  }

  private void validateRoomStatusForSave(
      RoomStatusEntity roomStatusEntity, RoomStatusEntity existingRoomStatus) {
    validationUtil.require(roomStatusEntity.getStatus() != null, "Room status is required");
  }
}
