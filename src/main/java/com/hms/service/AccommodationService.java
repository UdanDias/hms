package com.hms.service;

import com.hms.dto.AccommodationDTO;
import java.util.List;

public interface AccommodationService {
  AccommodationDTO getSelectedAccommodation(Long accommodationId);

  List<AccommodationDTO> getAllAccommodations();

  AccommodationDTO addAccommodation(AccommodationDTO dto);

  AccommodationDTO updateAccommodation(Long accommodationId, AccommodationDTO dto);
}
