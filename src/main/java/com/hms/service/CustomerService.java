package com.hms.service;

import com.hms.dto.CustomerDTO;
import java.util.List;

public interface CustomerService {
  CustomerDTO getSelectedCustomer(Long customerId);

  List<CustomerDTO> getAllCustomers();

  CustomerDTO addCustomer(CustomerDTO dto);

  CustomerDTO updateCustomer(Long customerId, CustomerDTO dto);

  void deleteCustomer(Long customerId);
}
