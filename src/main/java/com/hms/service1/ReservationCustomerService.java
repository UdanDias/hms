package com.hms.service;

import com.hms.dto.ReservationCustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationCustomerService {
  ReservationCustomerDTO getSelectedReservationCustomer(Long reservationCustomerId);
  Page<ReservationCustomerDTO> getAllReservationCustomers(Pageable pageable);

  ReservationCustomerDTO addReservationCustomer(ReservationCustomerDTO dto);

  ReservationCustomerDTO updateReservationCustomer(Long id, ReservationCustomerDTO dto);

  void deleteReservationCustomer(Long id);

  default String healthCheck() { return "ReservationCustomer service is running"; }
}
