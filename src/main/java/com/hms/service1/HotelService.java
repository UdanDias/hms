package com.hms.service;

import com.hms.dto.HotelDTO;
import java.util.List;

public interface HotelService {
  HotelDTO getSelectedHotel(Long hotelId);
  List<HotelDTO> getAllHotels();

  HotelDTO addHotel(HotelDTO dto);

  HotelDTO updateHotel(Long id, HotelDTO dto);

  default String healthCheck() { return "Hotel service is running"; }

  void deleteHotel(Long id);
}
