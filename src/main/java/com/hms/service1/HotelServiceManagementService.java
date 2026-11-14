package com.hms.service;

import com.hms.dto.HotelServiceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelServiceManagementService {
  HotelServiceDTO getSelectedHotelService(Long hotelServiceId);
  Page<HotelServiceDTO> getAllHotelServices(Pageable pageable);

  HotelServiceDTO addHotelService(HotelServiceDTO dto);

  HotelServiceDTO updateHotelService(Long id, HotelServiceDTO dto);

  default String healthCheck() { return "HotelService service is running"; }

  void deleteHotelService(Long id);
}
