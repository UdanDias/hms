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
public class PaymentServiceImpl implements PaymentService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BillingRules billing;

  @Override
  public PaymentDTO getSelectedPayment(Long paymentId) {
    s.actor();
    var e = dao.require(PaymentEntity.class, paymentId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<PaymentDTO> getAllPayments() {
    s.actor();
    return dao.all(PaymentEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public PaymentDTO addPayment(PaymentDTO dto) {
    s.finance();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  private void prepare(PaymentEntity e, PaymentEntity old) {
    billing.preparePayment(e);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public PaymentDTO allocatePayment(Long paymentId, Long invoiceId) {
    s.finance();
    var e = dao.require(PaymentEntity.class, paymentId);
    billing.allocate(e, dao.require(InvoiceEntity.class, invoiceId));
    return finish(e, "ALLOCATE");
  }

  private PaymentDTO finish(PaymentEntity e, String action) {
    s.validate(e);
    s.audit(action, "Payment", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
