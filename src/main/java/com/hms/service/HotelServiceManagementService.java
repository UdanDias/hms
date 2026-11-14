package com.hms.service;

import com.hms.dto.HotelServiceDTO;
import java.util.List;

public interface HotelServiceManagementService {
  HotelServiceDTO getSelectedHotelService(Long hotelServiceId);

  List<HotelServiceDTO> getAllHotelServices();

  HotelServiceDTO addHotelService(HotelServiceDTO dto);

  HotelServiceDTO updateHotelService(Long id, HotelServiceDTO dto);
}
