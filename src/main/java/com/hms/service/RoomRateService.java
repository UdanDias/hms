package com.hms.service;

import com.hms.dto.RoomRateDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface RoomRateService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    RoomRateDTO getSelectedRoomRate(Long roomRateId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<RoomRateDTO> getAllRoomRates();
}
