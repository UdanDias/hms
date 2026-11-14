package com.hms.service;

import com.hms.dto.ReservationDTO;
import java.util.List;

public interface ReservationService {
  ReservationDTO getSelectedReservation(Long reservationId);

  List<ReservationDTO> getAllReservations();

  ReservationDTO addReservation(ReservationDTO dto);

  ReservationDTO updateReservation(Long reservationId, ReservationDTO dto);

  ReservationDTO confirmReservation(Long reservationId);

  ReservationDTO cancelReservation(Long reservationId, String reason);

  ReservationDTO checkInReservation(Long reservationId);

  ReservationDTO checkOutReservation(Long reservationId);
}
