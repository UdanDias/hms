package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dao.RoomDAO;
import com.hms.dao.RoomStatusDAO;
import com.hms.dao.RoomTypeDAO;
import com.hms.dto.RoomDTO;
import com.hms.entities.HotelEntity;
import com.hms.entities.RoomEntity;
import com.hms.entities.RoomStatusEntity;
import com.hms.entities.RoomTypeEntity;
import com.hms.exception.HotelNotFoundException;
import com.hms.exception.RoomNotFoundException;
import com.hms.exception.RoomStatusNotFoundException;
import com.hms.exception.RoomTypeNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.RoomService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.UtilData;
import com.hms.util.ValidationUtil;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class RoomServiceImpl implements RoomService {
  private final HotelDAO hotelDao;
  private final RoomTypeDAO roomTypeDao;
  private final RoomDAO roomDao;
  private final RoomStatusDAO roomStatusDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public RoomDTO getSelectedRoom(Long roomId) {
    serviceSecurity.getCurrentUser();
    RoomEntity roomEntity =
        roomDao.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
    serviceSecurity.checkRead(roomEntity);
    return entityDTOConvert.convertRoomEntityToRoomDTO(roomEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomDTO> getAllRooms() {
    serviceSecurity.getCurrentUser();
    List<RoomEntity> entities =
        roomDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertRoomEntityListToRoomDTOList(entities);
  }

  @Override
  public RoomDTO addRoom(RoomDTO roomDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomEntity roomEntity = entityDTOConvert.convertRoomDTOToRoomEntity(roomDTO);
    if (roomDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(roomDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      roomEntity.setHotel(hotel);
    }
    if (roomDTO.getRoomTypeId() != null) {
      RoomTypeEntity roomType =
          roomTypeDao
              .findById(roomDTO.getRoomTypeId())
              .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
      roomEntity.setRoomType(roomType);
    }
    if (roomDTO.getStatusId() != null) {
      RoomStatusEntity roomStatus =
          roomStatusDao
              .findById(roomDTO.getStatusId())
              .orElseThrow(() -> new RoomStatusNotFoundException("RoomStatus not found"));
      roomEntity.setRoomStatus(roomStatus);
    }

    validateRoomForSave(roomEntity, null);
    validationUtil.validate(roomEntity);
    roomDao.save(roomEntity);
    auditRecorder.recordAction("CREATE", "Room", roomEntity.getId());
    roomDao.flush();
    return entityDTOConvert.convertRoomEntityToRoomDTO(roomEntity);
  }

  @Override
  public RoomDTO updateRoom(Long roomId, RoomDTO roomDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    RoomEntity roomEntity =
        roomDao.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
    RoomEntity requestedRoom = entityDTOConvert.convertRoomDTOToRoomEntity(roomDTO);
    if (roomDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(roomDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      requestedRoom.setHotel(hotel);
    }
    if (roomDTO.getRoomTypeId() != null) {
      RoomTypeEntity roomType =
          roomTypeDao
              .findById(roomDTO.getRoomTypeId())
              .orElseThrow(() -> new RoomTypeNotFoundException("RoomType not found"));
      requestedRoom.setRoomType(roomType);
    }
    if (roomDTO.getStatusId() != null) {
      RoomStatusEntity roomStatus =
          roomStatusDao
              .findById(roomDTO.getStatusId())
              .orElseThrow(() -> new RoomStatusNotFoundException("RoomStatus not found"));
      requestedRoom.setRoomStatus(roomStatus);
    }

    validateRoomForSave(requestedRoom, roomEntity);
    validationUtil.validate(requestedRoom);
    roomEntity.setHotel(requestedRoom.getHotel());
    roomEntity.setRoomType(requestedRoom.getRoomType());
    roomEntity.setRoomNumber(requestedRoom.getRoomNumber());
    roomEntity.setFloor(requestedRoom.getFloor());
    roomEntity.setOperationalStatus(requestedRoom.getOperationalStatus());
    roomEntity.setHousekeepingStatus(requestedRoom.getHousekeepingStatus());
    roomEntity.setNotes(requestedRoom.getNotes());
    roomEntity.setRoomStatus(requestedRoom.getRoomStatus());
    validationUtil.validate(roomEntity);
    roomDao.save(roomEntity);
    auditRecorder.recordAction("UPDATE", "Room", roomEntity.getId());
    roomDao.flush();
    return entityDTOConvert.convertRoomEntityToRoomDTO(roomEntity);
  }

  private void validateRoomForSave(RoomEntity roomEntity, RoomEntity existingRoom) {
    if (roomEntity.getNotes() == null) roomEntity.setNotes("");
    validationUtil.require(
        roomEntity.getHotel() != null
            && roomEntity.getRoomType() != null
            && roomEntity.getRoomStatus() != null,
        "Hotel, room type and room status are required");
    validationUtil.same(
        roomEntity.getHotel().getId(), roomEntity.getRoomType().getHotel().getId(), "Room type");
    validationUtil.text(roomEntity.getRoomNumber(), "Room number");
    if (roomEntity.getOperationalStatus() == null) roomEntity.setOperationalStatus("active");
    if (roomEntity.getHousekeepingStatus() == null) roomEntity.setHousekeepingStatus("clean");
    validationUtil.require(
        Set.of("active", "out_of_service", "retired").contains(roomEntity.getOperationalStatus()),
        "Invalid operational status");
    validationUtil.require(
        Set.of("clean", "dirty", "inspecting").contains(roomEntity.getHousekeepingStatus()),
        "Invalid housekeeping status");
    if (existingRoom != null) {
      validationUtil.same(existingRoom.getHotel().getId(), roomEntity.getHotel().getId(), "Hotel");
      boolean inventoryChange =
          !existingRoom.getRoomType().getId().equals(roomEntity.getRoomType().getId())
              || !existingRoom.getOperationalStatus().equals(roomEntity.getOperationalStatus())
              || !existingRoom
                  .getRoomStatus()
                  .getStatusId()
                  .equals(roomEntity.getRoomStatus().getStatusId());
      if (inventoryChange)
        validationUtil.require(
            reservationRoomDao
                .findActiveStaysEndingAfter(
                    existingRoom.getRoomType().getId(),
                    UtilData.todayAtHotel(roomEntity.getHotel()))
                .isEmpty(),
            "Resolve active bookings for this room type before changing its inventory");
    }
  }

  @Override
  public RoomDTO updateHousekeepingStatus(Long roomId, String status) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST", "HOUSEKEEPING");
    hotelDao.lockAllForWrite();
    validationUtil.require(
        Set.of("clean", "dirty", "inspecting").contains(status), "Invalid housekeeping status");
    RoomEntity roomEntity =
        roomDao.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
    roomEntity.setHousekeepingStatus(status);
    validationUtil.validate(roomEntity);
    auditRecorder.recordAction("HOUSEKEEPING", "Room", roomEntity.getId());
    roomDao.flush();
    return entityDTOConvert.convertRoomEntityToRoomDTO(roomEntity);
  }
}
