package com.hms.service;

import com.hms.dto.BookingStatusHistoryDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface BookingStatusHistoryService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    BookingStatusHistoryDTO getSelectedBookingStatusHistory(Long bookingStatusHistoryId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<BookingStatusHistoryDTO> getAllBookingStatusHistories();
}
