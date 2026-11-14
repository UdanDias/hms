package com.hms.service;

import com.hms.dto.RoomRateDTO;
import java.util.List;

public interface RoomRateService {
  RoomRateDTO getSelectedRoomRate(Long roomRateId);

  List<RoomRateDTO> getAllRoomRates();

  RoomRateDTO addRoomRate(RoomRateDTO dto);

  RoomRateDTO updateRoomRate(Long id, RoomRateDTO dto);

  void deleteRoomRate(Long id);
}
