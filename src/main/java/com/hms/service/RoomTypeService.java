package com.hms.service;

import com.hms.dto.RoomTypeDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface RoomTypeService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    RoomTypeDTO getSelectedRoomType(Long roomTypeId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<RoomTypeDTO> getAllRoomTypes();
}
