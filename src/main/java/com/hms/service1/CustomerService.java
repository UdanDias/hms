package com.hms.service;

import com.hms.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
  CustomerDTO getSelectedCustomer(Long customerId);
  Page<CustomerDTO> getAllCustomers(Pageable pageable);

  CustomerDTO addCustomer(CustomerDTO dto);

  CustomerDTO updateCustomer(Long id, CustomerDTO dto);

  void deleteCustomer(Long id);

  default String healthCheck() { return "Customer service is running"; }
}
