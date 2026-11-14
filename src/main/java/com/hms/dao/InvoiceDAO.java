package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.InvoiceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Persistence access for InvoiceEntity; business decisions are made by the service layer. */
@Repository
public class InvoiceDAO extends MutableHibernateDAO<InvoiceEntity> {
  public InvoiceDAO() {
    super(InvoiceEntity.class, "id", InvoiceEntity::getId);
  }

  public List<InvoiceEntity> findByHotelId(Long hotelId) {
    positiveId(hotelId);
    return select("e.hotel.id = :p0", hotelId);
  }

  public List<InvoiceEntity> findByReservationId(Long reservationId) {
    positiveId(reservationId);
    return select("e.reservation.id = :p0", reservationId);
  }

  public List<InvoiceEntity> findByBilledCustomerId(Long billedCustomerId) {
    positiveId(billedCustomerId);
    return select("e.billedCustomer.id = :p0", billedCustomerId);
  }

  public List<InvoiceEntity> findByCreatedById(Long createdById) {
    positiveId(createdById);
    return select("e.createdBy.userId = :p0", createdById);
  }

  public Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber) {
    return selectOne("e.invoiceNumber = :p0", invoiceNumber);
  }

  public List<InvoiceEntity> findNonVoidByReservationId(Long reservationId) {
    positiveId(reservationId);
    return select("e.reservation.id = :p0 and e.status <> 'void'", reservationId);
  }

  public List<InvoiceEntity> findIssuedByReservationId(Long p0) {
    return select("e.reservation.id = :p0 and e.status = 'issued'", p0);
  }
}
