package com.hms.service;

import com.hms.dto.HotelServiceDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface HotelServiceManagementService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    HotelServiceDTO getSelectedHotelService(Long hotelServiceId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<HotelServiceDTO> getAllHotelServices();
}
