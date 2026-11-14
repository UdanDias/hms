package com.hms.service;

import com.hms.dto.RoomStatusDTO;
import java.util.List;

public interface RoomStatusService {
  RoomStatusDTO getSelectedRoomStatus(Long roomStatusId);

  List<RoomStatusDTO> getAllRoomStatuses();

  RoomStatusDTO addRoomStatus(RoomStatusDTO dto);
}
