package com.hms.service;

import com.hms.dto.RoomTypeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomTypeService {
  RoomTypeDTO getSelectedRoomType(Long roomTypeId);
  Page<RoomTypeDTO> getAllRoomTypes(Pageable pageable);

  RoomTypeDTO addRoomType(RoomTypeDTO dto);

  RoomTypeDTO updateRoomType(Long id, RoomTypeDTO dto);

  default String healthCheck() { return "RoomType service is running"; }

  void deleteRoomType(Long id);
}
