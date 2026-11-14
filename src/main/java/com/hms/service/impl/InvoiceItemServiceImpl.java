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
public class InvoiceItemServiceImpl implements InvoiceItemService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final BillingRules billing;

  @Override
  public InvoiceItemDTO getSelectedInvoiceItem(Long invoiceItemId) {
    s.actor();
    var e = dao.require(InvoiceItemEntity.class, invoiceItemId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<InvoiceItemDTO> getAllInvoiceItems() {
    s.actor();
    return dao.all(InvoiceItemEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InvoiceItemDTO addInvoiceItem(InvoiceItemDTO dto) {
    s.finance();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    billing.recalculate(e.getInvoice());
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InvoiceItemDTO updateInvoiceItem(Long id, InvoiceItemDTO dto) {
    s.finance();
    var old = dao.require(InvoiceItemEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    billing.recalculate(old.getInvoice());
    return finish(old, "UPDATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteInvoiceItem(Long id) {
    s.finance();
    var e = dao.require(InvoiceItemEntity.class, id);
    billing.draft(e.getInvoice());
    var invoice = e.getInvoice();
    s.audit("DELETE", "InvoiceItem", id);
    dao.remove(e);
    dao.flush();
    billing.recalculate(invoice);
  }

  private void prepare(InvoiceItemEntity e, InvoiceItemEntity old) {
    billing.prepareItem(e, old);
  }

  private InvoiceItemDTO finish(InvoiceItemEntity e, String action) {
    s.validate(e);
    s.audit(action, "InvoiceItem", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
