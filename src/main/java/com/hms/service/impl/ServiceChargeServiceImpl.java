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
public class ServiceChargeServiceImpl implements ServiceChargeService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BillingRules billing;

  @Override
  public ServiceChargeDTO getSelectedServiceCharge(Long serviceChargeId) {
    s.actor();
    var e = dao.require(ServiceChargeEntity.class, serviceChargeId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<ServiceChargeDTO> getAllServiceCharges() {
    s.actor();
    return dao.all(ServiceChargeEntity.class).stream()
        .filter(s::visible)
        .map(mapper::toDTO)
        .toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ServiceChargeDTO addServiceCharge(ServiceChargeDTO dto) {
    s.write();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ServiceChargeDTO updateServiceCharge(Long id, ServiceChargeDTO dto) {
    s.write();
    var old = dao.require(ServiceChargeEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(ServiceChargeEntity e, ServiceChargeEntity old) {
    billing.prepareCharge(e, old);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ServiceChargeDTO voidServiceCharge(Long serviceChargeId) {
    s.write();
    var e = dao.require(ServiceChargeEntity.class, serviceChargeId);
    billing.unbilled(e);
    s.require(
        !"checked_out".equals(e.getReservation().getStatus()),
        "Cannot change charges after checkout");
    s.require("active".equals(e.getStatus()), "Charge is already void");
    e.setStatus("void");
    return finish(e, "VOID");
  }

  private ServiceChargeDTO finish(ServiceChargeEntity e, String action) {
    s.validate(e);
    s.audit(action, "ServiceCharge", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
