package com.hms.service;

import com.hms.dto.BookingStatusHistoryDTO;
import java.util.List;

public interface BookingStatusHistoryService {
  BookingStatusHistoryDTO getSelectedBookingStatusHistory(Long bookingStatusHistoryId);

  List<BookingStatusHistoryDTO> getAllBookingStatusHistories();
}
