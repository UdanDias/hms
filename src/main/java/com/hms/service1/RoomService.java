package com.hms.service;

import com.hms.dto.RoomDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {
  RoomDTO getSelectedRoom(Long roomId);
  Page<RoomDTO> getAllRooms(Pageable pageable);

  RoomDTO addRoom(RoomDTO dto);

  RoomDTO updateRoom(Long id, RoomDTO dto);

  RoomDTO updateHousekeepingStatus(Long roomId, String status);

  default String healthCheck() { return "Room service is running"; }

  void deleteRoom(Long id);
}
