package com.hms.service;

import com.hms.dto.ReservationDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface ReservationService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    ReservationDTO getSelectedReservation(Long reservationId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<ReservationDTO> getAllReservations();
}
