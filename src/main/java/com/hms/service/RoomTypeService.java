package com.hms.service;

import com.hms.dto.RoomTypeDTO;
import java.util.List;

public interface RoomTypeService {
  RoomTypeDTO getSelectedRoomType(Long roomTypeId);

  List<RoomTypeDTO> getAllRoomTypes();

  RoomTypeDTO addRoomType(RoomTypeDTO dto);

  RoomTypeDTO updateRoomType(Long id, RoomTypeDTO dto);
}
