package com.hms.service;

import com.hms.dto.RoomStatusDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomStatusService {
  RoomStatusDTO getSelectedRoomStatus(Long roomStatusId);
  Page<RoomStatusDTO> getAllRoomStatuses(Pageable pageable);

  RoomStatusDTO addRoomStatus(RoomStatusDTO dto);

  default String healthCheck() { return "RoomStatus service is running"; }

  RoomStatusDTO updateRoomStatus(Long id, RoomStatusDTO dto);

  void deleteRoomStatus(Long id);
}
