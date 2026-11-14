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
public class AccommodationServiceImpl implements AccommodationService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public AccommodationDTO getSelectedAccommodation(Long accommodationId) {
    s.actor();
    var e = dao.require(AccommodationEntity.class, accommodationId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<AccommodationDTO> getAllAccommodations() {
    s.actor();
    return dao.all(AccommodationEntity.class).stream()
        .filter(s::visible)
        .map(mapper::toDTO)
        .toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public AccommodationDTO addAccommodation(AccommodationDTO dto) {
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
  public AccommodationDTO updateAccommodation(Long id, AccommodationDTO dto) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    var old = dao.require(AccommodationEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(AccommodationEntity e, AccommodationEntity old) {
    s.require(
        e.getHotel() != null && e.getType() != null, "Hotel and accommodation type are required");
    e.setPrice(s.nonnegative(e.getPrice()));
    if (old != null) {
      s.same(old.getHotel().getId(), e.getHotel().getId(), "Hotel");
      s.require(
          old.getType() == e.getType(),
          "Accommodation type is immutable; add a new option instead");
    }
  }

  private AccommodationDTO finish(AccommodationEntity e, String action) {
    s.validate(e);
    s.audit(action, "Accommodation", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
