package com.hms.service;

import com.hms.dto.ReservationCustomerDTO;
import java.util.List;

public interface ReservationCustomerService {
  ReservationCustomerDTO getSelectedReservationCustomer(Long reservationCustomerId);

  List<ReservationCustomerDTO> getAllReservationCustomers();

  ReservationCustomerDTO addReservationCustomer(ReservationCustomerDTO dto);

  ReservationCustomerDTO updateReservationCustomer(Long id, ReservationCustomerDTO dto);

  void deleteReservationCustomer(Long id);
}
