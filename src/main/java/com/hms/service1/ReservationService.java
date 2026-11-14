package com.hms.service;

import com.hms.dto.ReservationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
  ReservationDTO getSelectedReservation(Long reservationId);
  Page<ReservationDTO> getAllReservations(Pageable pageable);

  ReservationDTO addReservation(ReservationDTO dto);

  ReservationDTO updateReservation(Long id, ReservationDTO dto);

  ReservationDTO confirmReservation(Long reservationId);

  ReservationDTO cancelReservation(Long reservationId, String reason);

  ReservationDTO checkInReservation(Long reservationId);

  ReservationDTO checkOutReservation(Long reservationId);

  default String healthCheck() { return "Reservation service is running"; }

  void deleteReservation(Long id);
}
