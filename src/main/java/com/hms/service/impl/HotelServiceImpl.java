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
public class HotelServiceImpl implements HotelService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public HotelDTO getSelectedHotel(Long hotelId) {
    s.actor();
    var e = dao.require(HotelEntity.class, hotelId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<HotelDTO> getAllHotels() {
    s.actor();
    return dao.all(HotelEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public HotelDTO addHotel(HotelDTO dto) {
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
  public HotelDTO updateHotel(Long id, HotelDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var old = dao.require(HotelEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(HotelEntity e, HotelEntity old) {
    s.text(e.getName(), "Hotel name");
    s.text(e.getTimezone(), "Hotel timezone");
    ZoneId.of(e.getTimezone());
    s.text(e.getCurrencyCode(), "Currency");
    e.setCurrencyCode(e.getCurrencyCode().toUpperCase(Locale.ROOT));
    java.util.Currency.getInstance(e.getCurrencyCode());
    s.require(
        Set.of("USD", "EUR", "GBP", "LKR", "AUD", "CAD", "INR", "NZD", "SGD", "AED")
            .contains(e.getCurrencyCode()),
        "This two-decimal money model currently supports USD/EUR/GBP/LKR/AUD/CAD/INR/NZD/SGD/AED");
    if (e.getIsActive() == null) e.setIsActive(true);
    if (old != null && !old.getCurrencyCode().equals(e.getCurrencyCode()))
      s.require(
          dao.query(ReservationEntity.class, "e.hotel.id = :p0", old.getId()).isEmpty(),
          "Currency cannot change after bookings exist");
  }

  private HotelDTO finish(HotelEntity e, String action) {
    s.validate(e);
    s.audit(action, "Hotel", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
