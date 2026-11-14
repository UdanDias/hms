package com.hms.service;

import com.hms.dto.AccommodationDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface AccommodationService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    AccommodationDTO getSelectedAccommodation(Long accommodationId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<AccommodationDTO> getAllAccommodations();
}
