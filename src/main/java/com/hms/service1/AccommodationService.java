package com.hms.service;

import com.hms.dto.AccommodationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
  AccommodationDTO getSelectedAccommodation(Long accommodationId);
  Page<AccommodationDTO> getAllAccommodations(Pageable pageable);

  AccommodationDTO addAccommodation(AccommodationDTO dto);

  AccommodationDTO updateAccommodation(Long id, AccommodationDTO dto);

  default String healthCheck() { return "Accommodation service is running"; }

  void deleteAccommodation(Long id);
}
