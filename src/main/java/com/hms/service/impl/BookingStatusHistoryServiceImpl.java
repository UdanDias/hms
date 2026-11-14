package com.hms.service.impl;

import com.hms.dao.BookingStatusHistoryDAO;
import com.hms.dto.BookingStatusHistoryDTO;
import com.hms.entities.BookingStatusHistoryEntity;
import com.hms.exception.BookingStatusHistoryNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.BookingStatusHistoryService;
import com.hms.util.EntityDTOConvert;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class BookingStatusHistoryServiceImpl implements BookingStatusHistoryService {
  private final BookingStatusHistoryDAO bookingStatusHistoryDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;

  @Override
  @Transactional(readOnly = true)
  public BookingStatusHistoryDTO getSelectedBookingStatusHistory(Long bookingStatusHistoryId) {
    serviceSecurity.getCurrentUser();
    BookingStatusHistoryEntity bookingStatusHistoryEntity =
        bookingStatusHistoryDao
            .findById(bookingStatusHistoryId)
            .orElseThrow(
                () -> new BookingStatusHistoryNotFoundException("BookingStatusHistory not found"));
    serviceSecurity.checkRead(bookingStatusHistoryEntity);
    return entityDTOConvert.convertBookingStatusHistoryEntityToBookingStatusHistoryDTO(
        bookingStatusHistoryEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookingStatusHistoryDTO> getAllBookingStatusHistories() {
    serviceSecurity.getCurrentUser();
    List<BookingStatusHistoryEntity> entities =
        bookingStatusHistoryDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertBookingStatusHistoryEntityListToBookingStatusHistoryDTOList(
        entities);
  }
}
