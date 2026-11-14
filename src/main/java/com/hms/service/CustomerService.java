package com.hms.service;

import com.hms.dto.CustomerDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface CustomerService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    CustomerDTO getSelectedCustomer(Long customerId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<CustomerDTO> getAllCustomers();
}
