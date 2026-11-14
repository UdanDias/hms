package com.hms.service;

import com.hms.dto.ReservationRoomDTO;
import java.util.List;

public interface ReservationRoomService {
  ReservationRoomDTO getSelectedReservationRoom(Long reservationRoomId);

  List<ReservationRoomDTO> getAllReservationRooms();

  ReservationRoomDTO addReservationRoom(ReservationRoomDTO dto);

  ReservationRoomDTO updateReservationRoom(Long id, ReservationRoomDTO dto);

  ReservationRoomDTO cancelReservationRoom(Long reservationRoomId, String reason);

  ReservationRoomDTO assignRoom(Long reservationRoomId, Long roomId);
}
