package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.CustomerDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * HMS business service for Customer.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {
    private final CustomerDao customerDao;
    private final UserDao userDao;

    private CustomerEntity getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Customer ID");
        return customerDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private CustomerDTO toDto(CustomerEntity entity) {
        CustomerDTO dto = new CustomerDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setUserId(entity.getUser() == null ? null : entity.getUser().getUserId());
        dto.setNic(null); // Sensitive fields are never echoed in standard responses.
        dto.setIdentityDocumentNumberEncrypted(null);
        return dto;
    }

    private void apply(CustomerDTO dto, CustomerEntity entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getAge() != null) entity.setAge(dto.getAge());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getCountryCode() != null) entity.setCountryCode(dto.getCountryCode());
        if (dto.getDateOfBirth() != null) entity.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getIdentityDocumentType() != null) entity.setIdentityDocumentType(dto.getIdentityDocumentType());
        if (dto.getIdentityDocumentNumberEncrypted() != null)
            throw new IllegalArgumentException("Identity-document encryption must occur in a trusted server-side service");
        if (dto.getUserId() != null) entity.setUser(
                    userDao.findById(dto.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUserId())));
    }

    @Override
    public CustomerDTO getSelectedCustomer(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return customerDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public CustomerDTO addCustomer(CustomerDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Customer DTO is required");
        if (dto.getNic() != null && !dto.getNic().isBlank()) throw new IllegalArgumentException("Configure server-side NIC encryption before storing NIC values");
        CustomerEntity entity = new CustomerEntity();
        apply(dto, entity);
        // Nothing extra.
        entity = customerDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Customer DTO is required");
        CustomerEntity entity = getEntity(id);
        if (dto.getNic() != null && !dto.getNic().isBlank()) throw new IllegalArgumentException("Configure server-side NIC encryption before storing NIC values");
        apply(dto, entity);
        // Nothing extra.
        entity = customerDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        CustomerEntity entity = getEntity(id);
        // Nothing extra.
        customerDao.delete(entity);
        customerDao.flush();
        // Nothing extra.
    }


}
