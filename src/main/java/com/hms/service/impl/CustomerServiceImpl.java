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
public class CustomerServiceImpl implements CustomerService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public CustomerDTO getSelectedCustomer(Long customerId) {
    s.actor();
    var e = dao.require(CustomerEntity.class, customerId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<CustomerDTO> getAllCustomers() {
    s.actor();
    return dao.all(CustomerEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public CustomerDTO addCustomer(CustomerDTO dto) {
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
  public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
    s.write();
    var old = dao.require(CustomerEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteCustomer(Long id) {
    s.write();
    var e = dao.require(CustomerEntity.class, id);
    s.audit("DELETE", "Customer", id);
    dao.remove(e);
    dao.flush();
  }

  private void prepare(CustomerEntity e, CustomerEntity old) {
    s.text(e.getName(), "Customer name");
    s.text(e.getPhone(), "Phone");
    if (e.getEmail() != null) e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
    if (e.getDateOfBirth() != null) {
      s.require(
          !e.getDateOfBirth().isAfter(LocalDate.now(ZoneOffset.UTC)),
          "Date of birth cannot be in the future");
      e.setAge(Period.between(e.getDateOfBirth(), LocalDate.now(ZoneOffset.UTC)).getYears());
    }
    s.require(
        e.getAge() == null || (e.getAge() >= 0 && e.getAge() <= 130),
        "Age must be between 0 and 130");
    s.require(
        e.getIdentityDocumentNumberEncrypted() == null,
        "Encrypted identity documents must be handled by a dedicated encryption service");
    if (old != null) {
      e.setIdentityDocumentNumberEncrypted(old.getIdentityDocumentNumberEncrypted());
      s.require(
          Objects.equals(
              old.getUser() == null ? null : old.getUser().getUserId(),
              e.getUser() == null ? null : e.getUser().getUserId()),
          "Existing customer-account links cannot be reassigned through an ordinary update");
    }
  }

  private CustomerDTO finish(CustomerEntity e, String action) {
    s.validate(e);
    s.audit(action, "Customer", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
