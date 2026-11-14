package com.hms.service;

import com.hms.dto.RoomRateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomRateService {
  RoomRateDTO getSelectedRoomRate(Long roomRateId);
  Page<RoomRateDTO> getAllRoomRates(Pageable pageable);

  RoomRateDTO addRoomRate(RoomRateDTO dto);

  RoomRateDTO updateRoomRate(Long id, RoomRateDTO dto);

  void deleteRoomRate(Long id);

  default String healthCheck() { return "RoomRate service is running"; }
}
