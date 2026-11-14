package com.hms.service;

import com.hms.dto.RoomDTO;
import java.util.List;

public interface RoomService {
  RoomDTO getSelectedRoom(Long roomId);

  List<RoomDTO> getAllRooms();

  RoomDTO addRoom(RoomDTO dto);

  RoomDTO updateRoom(Long id, RoomDTO dto);

  RoomDTO updateHousekeepingStatus(Long roomId, String status);
}
