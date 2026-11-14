package com.hms.service.impl;

import com.hms.dao.AccommodationDAO;
import com.hms.dao.HotelDAO;
import com.hms.dto.AccommodationDTO;
import com.hms.entities.AccommodationEntity;
import com.hms.entities.HotelEntity;
import com.hms.exception.AccommodationNotFoundException;
import com.hms.exception.HotelNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.AccommodationService;
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
public class AccommodationServiceImpl implements AccommodationService {
  private final HotelDAO hotelDao;
  private final AccommodationDAO accommodationDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public AccommodationDTO getSelectedAccommodation(Long accommodationId) {
    serviceSecurity.getCurrentUser();
    AccommodationEntity accommodationEntity =
        accommodationDao
            .findById(accommodationId)
            .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));
    serviceSecurity.checkRead(accommodationEntity);
    return entityDTOConvert.convertAccommodationEntityToAccommodationDTO(accommodationEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccommodationDTO> getAllAccommodations() {
    serviceSecurity.getCurrentUser();
    List<AccommodationEntity> entities =
        accommodationDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertAccommodationEntityListToAccommodationDTOList(entities);
  }

  @Override
  public AccommodationDTO addAccommodation(AccommodationDTO accommodationDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    AccommodationEntity accommodationEntity =
        entityDTOConvert.convertAccommodationDTOToAccommodationEntity(accommodationDTO);
    if (accommodationDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(accommodationDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      accommodationEntity.setHotel(hotel);
    }

    validateAccommodationForSave(accommodationEntity, null);
    validationUtil.validate(accommodationEntity);
    accommodationDao.save(accommodationEntity);
    auditRecorder.recordAction("CREATE", "Accommodation", accommodationEntity.getId());
    accommodationDao.flush();
    return entityDTOConvert.convertAccommodationEntityToAccommodationDTO(accommodationEntity);
  }

  @Override
  public AccommodationDTO updateAccommodation(
      Long accommodationId, AccommodationDTO accommodationDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    AccommodationEntity accommodationEntity =
        accommodationDao
            .findById(accommodationId)
            .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));
    AccommodationEntity requestedAccommodation =
        entityDTOConvert.convertAccommodationDTOToAccommodationEntity(accommodationDTO);
    if (accommodationDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(accommodationDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      requestedAccommodation.setHotel(hotel);
    }

    validateAccommodationForSave(requestedAccommodation, accommodationEntity);
    validationUtil.validate(requestedAccommodation);
    accommodationEntity.setHotel(requestedAccommodation.getHotel());
    accommodationEntity.setType(requestedAccommodation.getType());
    accommodationEntity.setPrice(requestedAccommodation.getPrice());
    validationUtil.validate(accommodationEntity);
    accommodationDao.save(accommodationEntity);
    auditRecorder.recordAction("UPDATE", "Accommodation", accommodationEntity.getId());
    accommodationDao.flush();
    return entityDTOConvert.convertAccommodationEntityToAccommodationDTO(accommodationEntity);
  }

  private void validateAccommodationForSave(
      AccommodationEntity accommodationEntity, AccommodationEntity existingAccommodation) {
    validationUtil.require(
        accommodationEntity.getHotel() != null && accommodationEntity.getType() != null,
        "Hotel and accommodation type are required");
    accommodationEntity.setPrice(MoneyUtil.nonnegative(accommodationEntity.getPrice()));
    if (existingAccommodation != null) {
      validationUtil.same(
          existingAccommodation.getHotel().getId(),
          accommodationEntity.getHotel().getId(),
          "Hotel");
      validationUtil.require(
          existingAccommodation.getType() == accommodationEntity.getType(),
          "Accommodation type is immutable; add a new option instead");
    }
  }
}
