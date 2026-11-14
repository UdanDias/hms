package com.hms.service;

import com.hms.dto.ReservationCustomerDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface ReservationCustomerService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    ReservationCustomerDTO getSelectedReservationCustomer(Long reservationCustomerId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<ReservationCustomerDTO> getAllReservationCustomers();
}
