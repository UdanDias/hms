package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.math.BigDecimal;
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
public class InvoiceServiceImpl implements InvoiceService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BillingRules billing;

  @Override
  public InvoiceDTO getSelectedInvoice(Long invoiceId) {
    s.actor();
    var e = dao.require(InvoiceEntity.class, invoiceId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<InvoiceDTO> getAllInvoices() {
    s.actor();
    return dao.all(InvoiceEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InvoiceDTO addInvoice(InvoiceDTO dto) {
    s.finance();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InvoiceDTO updateInvoice(Long id, InvoiceDTO dto) {
    s.finance();
    var old = dao.require(InvoiceEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(InvoiceEntity e, InvoiceEntity old) {
    billing.prepareInvoice(e, old);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InvoiceDTO issueInvoice(Long invoiceId) {
    s.finance();
    var e = dao.require(InvoiceEntity.class, invoiceId);
    billing.issue(e);
    return finish(e, "ISSUE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InvoiceDTO voidInvoice(Long invoiceId) {
    s.finance();
    var e = dao.require(InvoiceEntity.class, invoiceId);
    billing.voidInvoice(e);
    return finish(e, "VOID");
  }

  @Override
  public BigDecimal getOutstandingBalance(Long invoiceId) {
    s.actor();
    var e = dao.require(InvoiceEntity.class, invoiceId);
    s.checkRead(e);
    return billing.balance(e);
  }

  private InvoiceDTO finish(InvoiceEntity e, String action) {
    s.validate(e);
    s.audit(action, "Invoice", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
