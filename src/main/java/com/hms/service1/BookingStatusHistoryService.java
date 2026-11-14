package com.hms.service;

import com.hms.dto.BookingStatusHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingStatusHistoryService {
  BookingStatusHistoryDTO getSelectedBookingStatusHistory(Long bookingStatusHistoryId);
  Page<BookingStatusHistoryDTO> getAllBookingStatusHistories(Pageable pageable);

  default String healthCheck() { return "BookingStatusHistory service is running"; }

  BookingStatusHistoryDTO addBookingStatusHistory(BookingStatusHistoryDTO dto);

  BookingStatusHistoryDTO updateBookingStatusHistory(Long id, BookingStatusHistoryDTO dto);

  void deleteBookingStatusHistory(Long id);
}
