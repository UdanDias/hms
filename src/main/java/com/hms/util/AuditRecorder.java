package com.hms.util;

import com.hms.dao.AuditLogDAO;
import com.hms.dao.BookingStatusHistoryDAO;
import com.hms.entities.AuditLogEntity;
import com.hms.entities.BookingStatusHistoryEntity;
import com.hms.entities.ReservationEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.security.ServiceSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.*;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class AuditRecorder {
  private final AuditLogDAO auditLogDao;
  private final BookingStatusHistoryDAO bookingStatusHistoryDao;
  private final ServiceSecurity serviceSecurity;

  public void recordAction(String action, String entity, Long id) {
    var log = new AuditLogEntity();
    log.setActorUser(serviceSecurity.getCurrentUser());
    log.setAction(action);
    log.setEntityType(entity);
    log.setEntityId(id);
    log.setOccurredAt(UtilData.generateNow());
    auditLogDao.save(log);
  }

  public void recordBookingStatus(
      ReservationEntity reservation,
      ReservationRoomEntity room,
      String oldStatus,
      String status,
      String reason) {
    var h = new BookingStatusHistoryEntity();
    h.setReservation(reservation);
    h.setReservationRoom(room);
    h.setOldStatus(oldStatus);
    h.setNewStatus(status);
    h.setReason(reason);
    h.setChangedBy(serviceSecurity.getCurrentUser());
    h.setChangedAt(UtilData.generateNow());
    bookingStatusHistoryDao.save(h);
  }
}
