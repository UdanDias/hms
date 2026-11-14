package com.hms.service;

import com.hms.dto.RoomDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface RoomService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    RoomDTO getSelectedRoom(Long roomId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<RoomDTO> getAllRooms();
}
