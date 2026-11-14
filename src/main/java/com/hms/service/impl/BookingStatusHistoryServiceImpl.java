package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingStatusHistoryServiceImpl implements BookingStatusHistoryService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public BookingStatusHistoryDTO getSelectedBookingStatusHistory(Long bookingStatusHistoryId) {
    s.actor();
    var e = dao.require(BookingStatusHistoryEntity.class, bookingStatusHistoryId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<BookingStatusHistoryDTO> getAllBookingStatusHistories() {
    s.actor();
    return dao.all(BookingStatusHistoryEntity.class).stream()
        .filter(s::visible)
        .map(mapper::toDTO)
        .toList();
  }
}
