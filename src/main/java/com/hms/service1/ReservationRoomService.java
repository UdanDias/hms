package com.hms.service;

import com.hms.dto.ReservationRoomDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationRoomService {
  ReservationRoomDTO getSelectedReservationRoom(Long reservationRoomId);
  Page<ReservationRoomDTO> getAllReservationRooms(Pageable pageable);

  ReservationRoomDTO addReservationRoom(ReservationRoomDTO dto);

  ReservationRoomDTO updateReservationRoom(Long id, ReservationRoomDTO dto);

  ReservationRoomDTO cancelReservationRoom(Long reservationRoomId, String reason);

  ReservationRoomDTO assignRoom(Long reservationRoomId, Long roomId);

  default String healthCheck() { return "ReservationRoom service is running"; }

  void deleteReservationRoom(Long id);
}
