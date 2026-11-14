package com.hms.service;

import com.hms.dto.HotelDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface HotelService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    HotelDTO getSelectedHotel(Long hotelId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<HotelDTO> getAllHotels();
}
