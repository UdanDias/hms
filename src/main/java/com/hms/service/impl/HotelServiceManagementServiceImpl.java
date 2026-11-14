package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.HotelServiceDAO;
import com.hms.dto.HotelServiceDTO;
import com.hms.entities.HotelEntity;
import com.hms.entities.HotelServiceEntity;
import com.hms.exception.HotelNotFoundException;
import com.hms.exception.HotelServiceNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.HotelServiceManagementService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.MoneyUtil;
import com.hms.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class HotelServiceManagementServiceImpl implements HotelServiceManagementService {
  private final HotelDAO hotelDao;
  private final HotelServiceDAO hotelServiceDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public HotelServiceDTO getSelectedHotelService(Long hotelServiceId) {
    serviceSecurity.getCurrentUser();
    HotelServiceEntity hotelServiceEntity =
        hotelServiceDao
            .findById(hotelServiceId)
            .orElseThrow(() -> new HotelServiceNotFoundException("HotelService not found"));
    serviceSecurity.checkRead(hotelServiceEntity);
    return entityDTOConvert.convertHotelServiceEntityToHotelServiceDTO(hotelServiceEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<HotelServiceDTO> getAllHotelServices() {
    serviceSecurity.getCurrentUser();
    List<HotelServiceEntity> entities =
        hotelServiceDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertHotelServiceEntityListToHotelServiceDTOList(entities);
  }

  @Override
  public HotelServiceDTO addHotelService(HotelServiceDTO hotelServiceDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    HotelServiceEntity hotelServiceEntity =
        entityDTOConvert.convertHotelServiceDTOToHotelServiceEntity(hotelServiceDTO);
    if (hotelServiceDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(hotelServiceDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      hotelServiceEntity.setHotel(hotel);
    }

    validateHotelServiceForSave(hotelServiceEntity, null);
    validationUtil.validate(hotelServiceEntity);
    hotelServiceDao.save(hotelServiceEntity);
    auditRecorder.recordAction("CREATE", "HotelService", hotelServiceEntity.getId());
    hotelServiceDao.flush();
    return entityDTOConvert.convertHotelServiceEntityToHotelServiceDTO(hotelServiceEntity);
  }

  @Override
  public HotelServiceDTO updateHotelService(Long hotelServiceId, HotelServiceDTO hotelServiceDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    HotelServiceEntity hotelServiceEntity =
        hotelServiceDao
            .findById(hotelServiceId)
            .orElseThrow(() -> new HotelServiceNotFoundException("HotelService not found"));
    HotelServiceEntity requestedHotelService =
        entityDTOConvert.convertHotelServiceDTOToHotelServiceEntity(hotelServiceDTO);
    if (hotelServiceDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(hotelServiceDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      requestedHotelService.setHotel(hotel);
    }

    validateHotelServiceForSave(requestedHotelService, hotelServiceEntity);
    validationUtil.validate(requestedHotelService);
    hotelServiceEntity.setHotel(requestedHotelService.getHotel());
    hotelServiceEntity.setName(requestedHotelService.getName());
    hotelServiceEntity.setDescription(requestedHotelService.getDescription());
    hotelServiceEntity.setUnit(requestedHotelService.getUnit());
    hotelServiceEntity.setDefaultPrice(requestedHotelService.getDefaultPrice());
    hotelServiceEntity.setDefaultTaxRate(requestedHotelService.getDefaultTaxRate());
    hotelServiceEntity.setIsActive(requestedHotelService.getIsActive());
    validationUtil.validate(hotelServiceEntity);
    hotelServiceDao.save(hotelServiceEntity);
    auditRecorder.recordAction("UPDATE", "HotelService", hotelServiceEntity.getId());
    hotelServiceDao.flush();
    return entityDTOConvert.convertHotelServiceEntityToHotelServiceDTO(hotelServiceEntity);
  }

  private void validateHotelServiceForSave(
      HotelServiceEntity hotelServiceEntity, HotelServiceEntity existingHotelService) {
    if (hotelServiceEntity.getDescription() == null) hotelServiceEntity.setDescription("");
    validationUtil.require(hotelServiceEntity.getHotel() != null, "Hotel is required");
    validationUtil.text(hotelServiceEntity.getName(), "Service name");
    validationUtil.text(hotelServiceEntity.getUnit(), "Service unit");
    hotelServiceEntity.setDefaultPrice(MoneyUtil.nonnegative(hotelServiceEntity.getDefaultPrice()));
    hotelServiceEntity.setDefaultTaxRate(validateTaxRate(hotelServiceEntity.getDefaultTaxRate()));
    if (hotelServiceEntity.getIsActive() == null) hotelServiceEntity.setIsActive(true);
    if (existingHotelService != null)
      validationUtil.same(
          existingHotelService.getHotel().getId(), hotelServiceEntity.getHotel().getId(), "Hotel");
  }

  private BigDecimal validateTaxRate(BigDecimal value) {
    var x = value == null ? BigDecimal.ZERO : value;
    validationUtil.require(
        x.signum() >= 0 && x.compareTo(BigDecimal.valueOf(100)) <= 0,
        "Tax percentage must be between 0 and 100");
    return x;
  }
}
