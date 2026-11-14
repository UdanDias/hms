package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dto.HotelDTO;
import com.hms.entities.HotelEntity;
import com.hms.exception.HotelNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.HotelService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.ValidationUtil;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class HotelServiceImpl implements HotelService {
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public HotelDTO getSelectedHotel(Long hotelId) {
    serviceSecurity.getCurrentUser();
    HotelEntity hotelEntity =
        hotelDao.findById(hotelId).orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
    serviceSecurity.checkRead(hotelEntity);
    return entityDTOConvert.convertHotelEntityToHotelDTO(hotelEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<HotelDTO> getAllHotels() {
    serviceSecurity.getCurrentUser();
    List<HotelEntity> entities =
        hotelDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertHotelEntityListToHotelDTOList(entities);
  }

  @Override
  public HotelDTO addHotel(HotelDTO hotelDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    HotelEntity hotelEntity = entityDTOConvert.convertHotelDTOToHotelEntity(hotelDTO);

    validateHotelForSave(hotelEntity, null);
    validationUtil.validate(hotelEntity);
    hotelDao.save(hotelEntity);
    auditRecorder.recordAction("CREATE", "Hotel", hotelEntity.getId());
    hotelDao.flush();
    return entityDTOConvert.convertHotelEntityToHotelDTO(hotelEntity);
  }

  @Override
  public HotelDTO updateHotel(Long hotelId, HotelDTO hotelDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    HotelEntity hotelEntity =
        hotelDao.findById(hotelId).orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
    HotelEntity requestedHotel = entityDTOConvert.convertHotelDTOToHotelEntity(hotelDTO);

    validateHotelForSave(requestedHotel, hotelEntity);
    validationUtil.validate(requestedHotel);
    hotelEntity.setName(requestedHotel.getName());
    hotelEntity.setAddress(requestedHotel.getAddress());
    hotelEntity.setCity(requestedHotel.getCity());
    hotelEntity.setCountryCode(requestedHotel.getCountryCode());
    hotelEntity.setPhone(requestedHotel.getPhone());
    hotelEntity.setEmail(requestedHotel.getEmail());
    hotelEntity.setTimezone(requestedHotel.getTimezone());
    hotelEntity.setCurrencyCode(requestedHotel.getCurrencyCode());
    hotelEntity.setCheckInTime(requestedHotel.getCheckInTime());
    hotelEntity.setCheckOutTime(requestedHotel.getCheckOutTime());
    hotelEntity.setIsActive(requestedHotel.getIsActive());
    validationUtil.validate(hotelEntity);
    hotelDao.save(hotelEntity);
    auditRecorder.recordAction("UPDATE", "Hotel", hotelEntity.getId());
    hotelDao.flush();
    return entityDTOConvert.convertHotelEntityToHotelDTO(hotelEntity);
  }

  private void validateHotelForSave(HotelEntity hotelEntity, HotelEntity existingHotel) {
    validationUtil.text(hotelEntity.getName(), "Hotel name");
    validationUtil.text(hotelEntity.getTimezone(), "Hotel timezone");
    ZoneId.of(hotelEntity.getTimezone());
    validationUtil.text(hotelEntity.getCurrencyCode(), "Currency");
    hotelEntity.setCurrencyCode(hotelEntity.getCurrencyCode().toUpperCase(Locale.ROOT));
    java.util.Currency.getInstance(hotelEntity.getCurrencyCode());
    validationUtil.require(
        Set.of("USD", "EUR", "GBP", "LKR", "AUD", "CAD", "INR", "NZD", "SGD", "AED")
            .contains(hotelEntity.getCurrencyCode()),
        "This two-decimal money model currently supports USD/EUR/GBP/LKR/AUD/CAD/INR/NZD/SGD/AED");
    if (hotelEntity.getIsActive() == null) hotelEntity.setIsActive(true);
    if (existingHotel != null
        && !existingHotel.getCurrencyCode().equals(hotelEntity.getCurrencyCode()))
      validationUtil.require(
          reservationDao.findByHotelId(existingHotel.getId()).isEmpty(),
          "Currency cannot change after bookings exist");
  }
}
