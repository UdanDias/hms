package com.hms.service;

import com.hms.dto.ReservationRoomDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface ReservationRoomService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    ReservationRoomDTO getSelectedReservationRoom(Long reservationRoomId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<ReservationRoomDTO> getAllReservationRooms();
}
