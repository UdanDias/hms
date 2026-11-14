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
public class HotelServiceManagementServiceImpl implements HotelServiceManagementService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BillingRules billing;

  @Override
  public HotelServiceDTO getSelectedHotelService(Long hotelServiceId) {
    s.actor();
    var e = dao.require(HotelServiceEntity.class, hotelServiceId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<HotelServiceDTO> getAllHotelServices() {
    s.actor();
    return dao.all(HotelServiceEntity.class).stream()
        .filter(s::visible)
        .map(mapper::toDTO)
        .toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public HotelServiceDTO addHotelService(HotelServiceDTO dto) {
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
  public HotelServiceDTO updateHotelService(Long id, HotelServiceDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var old = dao.require(HotelServiceEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(HotelServiceEntity e, HotelServiceEntity old) {
    if (e.getDescription() == null) e.setDescription("");
    s.require(e.getHotel() != null, "Hotel is required");
    s.text(e.getName(), "Service name");
    s.text(e.getUnit(), "Service unit");
    e.setDefaultPrice(s.nonnegative(e.getDefaultPrice()));
    e.setDefaultTaxRate(billing.taxRate(e.getDefaultTaxRate()));
    if (e.getIsActive() == null) e.setIsActive(true);
    if (old != null) s.same(old.getHotel().getId(), e.getHotel().getId(), "Hotel");
  }

  private HotelServiceDTO finish(HotelServiceEntity e, String action) {
    s.validate(e);
    s.audit(action, "HotelService", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
