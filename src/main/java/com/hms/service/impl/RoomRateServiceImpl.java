package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.RoomRateDAO;
import com.hms.dao.RoomTypeDAO;
import com.hms.dto.RoomRateDTO;
import com.hms.entities.RoomRateEntity;
import com.hms.entities.RoomTypeEntity;
import com.hms.exception.RoomRateNotFoundException;
import com.hms.exception.RoomTypeNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.RoomRateService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.MoneyUtil;
import com.hms.util.ValidationUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class RoomRateServiceImpl implements RoomRateService {
  private final HotelDAO hotelDao;
  private final RoomTypeDAO roomTypeDao;
  private final RoomRateDAO roomRateDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public RoomRateDTO getSelectedRoomRate(Long roomRateId) {
    serviceSecurity.getCurrentUser();
    RoomRateEntity roomRateEntity =
        roomRateDao
            .findById(roomRateId)
            .orElseThrow(() -> new RoomRateNotFoundException("RoomRate not found"));
    serviceSecurity.checkRead(roomRateEntity);
    return entityDTOConvert.convertRoomRateEntityToRoomRateDTO(roomRateEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomRateDTO> getAllRoomRates() {
    serviceSecurity.getCurrentUser();
    List<RoomRateEntity> entities =
        roomRateDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertRoomRateEntityListToRoomRateDTOList(entities);
  }

  @Override
  public RoomRateDTO addRoomRate(RoomRateDTO roomRateDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomRateEntity roomRateEntity =
        entityDTOConvert.convertRoomRateDTOToRoomRateEntity(roomRateDTO);
    if (roomRateDTO.getRoomTypeId() != null) {
      RoomTypeEntity roomType =
          roomTypeDao
              .findById(roomRateDTO.getRoomTypeId())
              .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
      roomRateEntity.setRoomType(roomType);
    }

    validateRoomRateForSave(roomRateEntity, null);
    validationUtil.validate(roomRateEntity);
    roomRateDao.save(roomRateEntity);
    auditRecorder.recordAction("CREATE", "RoomRate", roomRateEntity.getId());
    roomRateDao.flush();
    return entityDTOConvert.convertRoomRateEntityToRoomRateDTO(roomRateEntity);
  }

  @Override
  public RoomRateDTO updateRoomRate(Long roomRateId, RoomRateDTO roomRateDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomRateEntity roomRateEntity =
        roomRateDao
            .findById(roomRateId)
            .orElseThrow(() -> new RoomRateNotFoundException("RoomRate not found"));
    RoomRateEntity requestedRoomRate =
        entityDTOConvert.convertRoomRateDTOToRoomRateEntity(roomRateDTO);
    if (roomRateDTO.getRoomTypeId() != null) {
      RoomTypeEntity roomType =
          roomTypeDao
              .findById(roomRateDTO.getRoomTypeId())
              .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
      requestedRoomRate.setRoomType(roomType);
    }

    validateRoomRateForSave(requestedRoomRate, roomRateEntity);
    validationUtil.validate(requestedRoomRate);
    roomRateEntity.setRoomType(requestedRoomRate.getRoomType());
    roomRateEntity.setStayDate(requestedRoomRate.getStayDate());
    roomRateEntity.setNightlyRate(requestedRoomRate.getNightlyRate());
    roomRateEntity.setExtraAdultRate(requestedRoomRate.getExtraAdultRate());
    roomRateEntity.setExtraChildRate(requestedRoomRate.getExtraChildRate());
    roomRateEntity.setMinimumNights(requestedRoomRate.getMinimumNights());
    roomRateEntity.setStopSell(requestedRoomRate.getStopSell());
    validationUtil.validate(roomRateEntity);
    roomRateDao.save(roomRateEntity);
    auditRecorder.recordAction("UPDATE", "RoomRate", roomRateEntity.getId());
    roomRateDao.flush();
    return entityDTOConvert.convertRoomRateEntityToRoomRateDTO(roomRateEntity);
  }

  @Override
  public void deleteRoomRate(Long roomRateId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomRateEntity roomRateEntity =
        roomRateDao
            .findById(roomRateId)
            .orElseThrow(() -> new RoomRateNotFoundException("RoomRate not found"));
    auditRecorder.recordAction("DELETE", "RoomRate", roomRateId);
    roomRateDao.delete(roomRateEntity);
    roomRateDao.flush();
  }

  private void validateRoomRateForSave(
      RoomRateEntity roomRateEntity, RoomRateEntity existingRoomRate) {
    validationUtil.require(
        roomRateEntity.getRoomType() != null && roomRateEntity.getStayDate() != null,
        "Room type and stay date are required");
    roomRateEntity.setNightlyRate(MoneyUtil.nonnegative(roomRateEntity.getNightlyRate()));
    roomRateEntity.setExtraAdultRate(MoneyUtil.nonnegative(roomRateEntity.getExtraAdultRate()));
    roomRateEntity.setExtraChildRate(MoneyUtil.nonnegative(roomRateEntity.getExtraChildRate()));
    if (roomRateEntity.getMinimumNights() == null) roomRateEntity.setMinimumNights(1);
    validationUtil.require(
        roomRateEntity.getMinimumNights() >= 1, "Minimum nights must be positive");
    if (roomRateEntity.getStopSell() == null) roomRateEntity.setStopSell(false);
    if (existingRoomRate != null)
      validationUtil.same(
          existingRoomRate.getRoomType().getId(),
          roomRateEntity.getRoomType().getId(),
          "Room type");
  }
}
