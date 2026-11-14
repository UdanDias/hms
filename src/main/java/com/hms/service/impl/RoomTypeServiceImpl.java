package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dao.RoomTypeDAO;
import com.hms.dto.RoomTypeDTO;
import com.hms.entities.HotelEntity;
import com.hms.entities.RoomTypeEntity;
import com.hms.exception.HotelNotFoundException;
import com.hms.exception.RoomTypeNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.RoomTypeService;
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
public class RoomTypeServiceImpl implements RoomTypeService {
  private final HotelDAO hotelDao;
  private final RoomTypeDAO roomTypeDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public RoomTypeDTO getSelectedRoomType(Long roomTypeId) {
    serviceSecurity.getCurrentUser();
    RoomTypeEntity roomTypeEntity =
        roomTypeDao
            .findById(roomTypeId)
            .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
    serviceSecurity.checkRead(roomTypeEntity);
    return entityDTOConvert.convertRoomTypeEntityToRoomTypeDTO(roomTypeEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomTypeDTO> getAllRoomTypes() {
    serviceSecurity.getCurrentUser();
    List<RoomTypeEntity> entities =
        roomTypeDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertRoomTypeEntityListToRoomTypeDTOList(entities);
  }

  @Override
  public RoomTypeDTO addRoomType(RoomTypeDTO roomTypeDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomTypeEntity roomTypeEntity =
        entityDTOConvert.convertRoomTypeDTOToRoomTypeEntity(roomTypeDTO);
    if (roomTypeDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(roomTypeDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      roomTypeEntity.setHotel(hotel);
    }

    validateRoomTypeForSave(roomTypeEntity, null);
    validationUtil.validate(roomTypeEntity);
    roomTypeDao.save(roomTypeEntity);
    auditRecorder.recordAction("CREATE", "RoomType", roomTypeEntity.getId());
    roomTypeDao.flush();
    return entityDTOConvert.convertRoomTypeEntityToRoomTypeDTO(roomTypeEntity);
  }

  @Override
  public RoomTypeDTO updateRoomType(Long roomTypeId, RoomTypeDTO roomTypeDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomTypeEntity roomTypeEntity =
        roomTypeDao
            .findById(roomTypeId)
            .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
    RoomTypeEntity requestedRoomType =
        entityDTOConvert.convertRoomTypeDTOToRoomTypeEntity(roomTypeDTO);
    if (roomTypeDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(roomTypeDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      requestedRoomType.setHotel(hotel);
    }

    validateRoomTypeForSave(requestedRoomType, roomTypeEntity);
    validationUtil.validate(requestedRoomType);
    roomTypeEntity.setHotel(requestedRoomType.getHotel());
    roomTypeEntity.setName(requestedRoomType.getName());
    roomTypeEntity.setDescription(requestedRoomType.getDescription());
    roomTypeEntity.setMaxAdults(requestedRoomType.getMaxAdults());
    roomTypeEntity.setMaxChildren(requestedRoomType.getMaxChildren());
    roomTypeEntity.setMaxOccupancy(requestedRoomType.getMaxOccupancy());
    roomTypeEntity.setBedDescription(requestedRoomType.getBedDescription());
    roomTypeEntity.setIsActive(requestedRoomType.getIsActive());
    validationUtil.validate(roomTypeEntity);
    roomTypeDao.save(roomTypeEntity);
    auditRecorder.recordAction("UPDATE", "RoomType", roomTypeEntity.getId());
    roomTypeDao.flush();
    return entityDTOConvert.convertRoomTypeEntityToRoomTypeDTO(roomTypeEntity);
  }

  private void validateRoomTypeForSave(
      RoomTypeEntity roomTypeEntity, RoomTypeEntity existingRoomType) {
    if (roomTypeEntity.getDescription() == null) roomTypeEntity.setDescription("");
    if (roomTypeEntity.getBedDescription() == null) roomTypeEntity.setBedDescription("");
    validationUtil.require(roomTypeEntity.getHotel() != null, "Hotel is required");
    validationUtil.text(roomTypeEntity.getName(), "Room type name");
    validationUtil.require(
        roomTypeEntity.getMaxAdults() != null
            && roomTypeEntity.getMaxAdults() >= 1
            && roomTypeEntity.getMaxChildren() != null
            && roomTypeEntity.getMaxChildren() >= 0
            && roomTypeEntity.getMaxOccupancy() != null
            && roomTypeEntity.getMaxOccupancy() >= 1
            && roomTypeEntity.getMaxAdults() <= roomTypeEntity.getMaxOccupancy(),
        "Invalid occupancy limits");
    if (roomTypeEntity.getIsActive() == null) roomTypeEntity.setIsActive(true);
    if (existingRoomType != null) {
      validationUtil.same(
          existingRoomType.getHotel().getId(), roomTypeEntity.getHotel().getId(), "Hotel");
      for (var stay : reservationRoomDao.findActiveByRoomTypeId(existingRoomType.getId()))
        validationUtil.require(
            stay.getAdults() <= roomTypeEntity.getMaxAdults()
                && stay.getChildren() <= roomTypeEntity.getMaxChildren()
                && stay.getAdults() + stay.getChildren() <= roomTypeEntity.getMaxOccupancy(),
            "New occupancy limits conflict with active bookings");
    }
  }
}
