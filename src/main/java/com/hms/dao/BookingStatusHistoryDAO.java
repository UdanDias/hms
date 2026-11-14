package com.hms.dao;

import com.hms.dao.support.AbstractHibernateDAO;
import com.hms.entities.BookingStatusHistoryEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for BookingStatusHistoryEntity; business decisions are made by the service
 * layer.
 */
@Repository
public class BookingStatusHistoryDAO extends AbstractHibernateDAO<BookingStatusHistoryEntity> {
  public BookingStatusHistoryDAO() {
    super(BookingStatusHistoryEntity.class, "id", BookingStatusHistoryEntity::getId);
  }

  public List<BookingStatusHistoryEntity> findByReservationId(Long reservationId) {
    positiveId(reservationId);
    return select("e.reservation.id = :p0", reservationId);
  }

  public List<BookingStatusHistoryEntity> findByReservationRoomId(Long reservationRoomId) {
    positiveId(reservationRoomId);
    return select("e.reservationRoom.id = :p0", reservationRoomId);
  }

  public List<BookingStatusHistoryEntity> findByChangedById(Long changedById) {
    positiveId(changedById);
    return select("e.changedBy.userId = :p0", changedById);
  }

  public List<BookingStatusHistoryEntity> findTimelineByReservationId(Long reservationId) {
    positiveId(reservationId);
    return selectOrdered("e.reservation.id = :p0", "e.changedAt, e.id", reservationId);
  }
}
