package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.ReservationCustomerEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for ReservationCustomerEntity; business decisions are made by the service
 * layer.
 */
@Repository
public class ReservationCustomerDAO extends MutableHibernateDAO<ReservationCustomerEntity> {
  public ReservationCustomerDAO() {
    super(ReservationCustomerEntity.class, "id", ReservationCustomerEntity::getId);
  }

  public List<ReservationCustomerEntity> findByReservationRoomId(Long reservationRoomId) {
    positiveId(reservationRoomId);
    return select("e.reservationRoom.id = :p0", reservationRoomId);
  }

  public List<ReservationCustomerEntity> findByCustomerId(Long customerId) {
    positiveId(customerId);
    return select("e.customer.id = :p0", customerId);
  }

  public Optional<ReservationCustomerEntity> findByReservationRoomIdAndCustomerId(
      Long reservationRoomId, Long customerId) {
    positiveId(reservationRoomId);
    positiveId(customerId);
    return selectOne(
        "e.reservationRoom.id = :p0 and e.customer.id = :p1", reservationRoomId, customerId);
  }

  public Optional<ReservationCustomerEntity> findPrimaryByReservationRoomId(
      Long reservationRoomId) {
    positiveId(reservationRoomId);
    return selectOne(
        "e.reservationRoom.id = :p0 and e.isPrimaryCustomer = true", reservationRoomId);
  }

  public List<ReservationCustomerEntity> findPrimaryGuestsByReservationRoomId(Long p0) {
    return select("e.reservationRoom.id = :p0 and e.isPrimaryCustomer = true", p0);
  }
}
