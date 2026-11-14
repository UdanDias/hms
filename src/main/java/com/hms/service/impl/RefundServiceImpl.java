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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundServiceImpl implements RefundService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BillingRules billing;

  @Override
  public RefundDTO getSelectedRefund(Long refundId) {
    s.actor();
    var e = dao.require(RefundEntity.class, refundId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<RefundDTO> getAllRefunds() {
    s.actor();
    return dao.all(RefundEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RefundDTO addRefund(RefundDTO dto) {
    s.finance();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  private void prepare(RefundEntity e, RefundEntity old) {
    billing.prepareRefund(e);
  }

  private RefundDTO finish(RefundEntity e, String action) {
    s.validate(e);
    s.audit(action, "Refund", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
