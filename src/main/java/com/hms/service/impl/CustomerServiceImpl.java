package com.hms.service.impl;

import com.hms.dao.CustomerDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.UserDAO;
import com.hms.dto.CustomerDTO;
import com.hms.entities.CustomerEntity;
import com.hms.entities.UserEntity;
import com.hms.exception.CustomerNotFoundException;
import com.hms.exception.UserNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.CustomerService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.ValidationUtil;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CustomerServiceImpl implements CustomerService {
  private final UserDAO userDao;
  private final CustomerDAO customerDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public CustomerDTO getSelectedCustomer(Long customerId) {
    serviceSecurity.getCurrentUser();
    CustomerEntity customerEntity =
        customerDao
            .findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    serviceSecurity.checkRead(customerEntity);
    return entityDTOConvert.convertCustomerEntityToCustomerDTO(customerEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerDTO> getAllCustomers() {
    serviceSecurity.getCurrentUser();
    List<CustomerEntity> entities =
        customerDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertCustomerEntityListToCustomerDTOList(entities);
  }

  @Override
  public CustomerDTO addCustomer(CustomerDTO customerDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    CustomerEntity customerEntity =
        entityDTOConvert.convertCustomerDTOToCustomerEntity(customerDTO);
    if (customerDTO.getUserId() != null) {
      UserEntity user =
          userDao
              .findById(customerDTO.getUserId())
              .orElseThrow(() -> new UserNotFoundException("User not found"));
      customerEntity.setUser(user);
    }

    validateCustomerForSave(customerEntity, null);
    validationUtil.validate(customerEntity);
    customerDao.save(customerEntity);
    auditRecorder.recordAction("CREATE", "Customer", customerEntity.getId());
    customerDao.flush();
    return entityDTOConvert.convertCustomerEntityToCustomerDTO(customerEntity);
  }

  @Override
  public CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    CustomerEntity customerEntity =
        customerDao
            .findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    CustomerEntity requestedCustomer =
        entityDTOConvert.convertCustomerDTOToCustomerEntity(customerDTO);
    if (customerDTO.getUserId() != null) {
      UserEntity user =
          userDao
              .findById(customerDTO.getUserId())
              .orElseThrow(() -> new UserNotFoundException("User not found"));
      requestedCustomer.setUser(user);
    }

    validateCustomerForSave(requestedCustomer, customerEntity);
    validationUtil.validate(requestedCustomer);
    customerEntity.setUser(requestedCustomer.getUser());
    customerEntity.setName(requestedCustomer.getName());
    customerEntity.setPhone(requestedCustomer.getPhone());
    customerEntity.setEmail(requestedCustomer.getEmail());
    customerEntity.setNic(requestedCustomer.getNic());
    customerEntity.setAge(requestedCustomer.getAge());
    customerEntity.setAddress(requestedCustomer.getAddress());
    customerEntity.setCountryCode(requestedCustomer.getCountryCode());
    customerEntity.setDateOfBirth(requestedCustomer.getDateOfBirth());
    customerEntity.setIdentityDocumentType(requestedCustomer.getIdentityDocumentType());
    customerEntity.setIdentityDocumentNumberEncrypted(
        requestedCustomer.getIdentityDocumentNumberEncrypted());
    validationUtil.validate(customerEntity);
    customerDao.save(customerEntity);
    auditRecorder.recordAction("UPDATE", "Customer", customerEntity.getId());
    customerDao.flush();
    return entityDTOConvert.convertCustomerEntityToCustomerDTO(customerEntity);
  }

  @Override
  public void deleteCustomer(Long customerId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    CustomerEntity customerEntity =
        customerDao
            .findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    auditRecorder.recordAction("DELETE", "Customer", customerId);
    customerDao.delete(customerEntity);
    customerDao.flush();
  }

  private void validateCustomerForSave(
      CustomerEntity customerEntity, CustomerEntity existingCustomer) {
    validationUtil.text(customerEntity.getName(), "Customer name");
    validationUtil.text(customerEntity.getPhone(), "Phone");
    if (customerEntity.getEmail() != null)
      customerEntity.setEmail(customerEntity.getEmail().trim().toLowerCase(Locale.ROOT));
    if (customerEntity.getDateOfBirth() != null) {
      validationUtil.require(
          !customerEntity.getDateOfBirth().isAfter(LocalDate.now(ZoneOffset.UTC)),
          "Date of birth cannot be in the future");
      customerEntity.setAge(
          Period.between(customerEntity.getDateOfBirth(), LocalDate.now(ZoneOffset.UTC))
              .getYears());
    }
    validationUtil.require(
        customerEntity.getAge() == null
            || (customerEntity.getAge() >= 0 && customerEntity.getAge() <= 130),
        "Age must be between 0 and 130");
    validationUtil.require(
        customerEntity.getIdentityDocumentNumberEncrypted() == null,
        "Encrypted identity documents must be handled by a dedicated encryption service");
    if (existingCustomer != null) {
      customerEntity.setIdentityDocumentNumberEncrypted(
          existingCustomer.getIdentityDocumentNumberEncrypted());
      validationUtil.require(
          Objects.equals(
              existingCustomer.getUser() == null ? null : existingCustomer.getUser().getUserId(),
              customerEntity.getUser() == null ? null : customerEntity.getUser().getUserId()),
          "Existing customer-account links cannot be reassigned through an ordinary update");
    }
  }
}
