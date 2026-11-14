package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.InvoiceItemEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Persistence access for InvoiceItemEntity; business decisions are made by the service layer. */
@Repository
public class InvoiceItemDAO extends MutableHibernateDAO<InvoiceItemEntity> {
  public InvoiceItemDAO() {
    super(InvoiceItemEntity.class, "id", InvoiceItemEntity::getId);
  }

  public List<InvoiceItemEntity> findByInvoiceId(Long invoiceId) {
    positiveId(invoiceId);
    return select("e.invoice.id = :p0", invoiceId);
  }

  public List<InvoiceItemEntity> findByReservationRoomId(Long reservationRoomId) {
    positiveId(reservationRoomId);
    return select("e.reservationRoom.id = :p0", reservationRoomId);
  }

  public List<InvoiceItemEntity> findByServiceChargeId(Long serviceChargeId) {
    positiveId(serviceChargeId);
    return select("e.serviceCharge.id = :p0", serviceChargeId);
  }

  public List<InvoiceItemEntity> findByAccommodationId(Long accommodationId) {
    positiveId(accommodationId);
    return select("e.accommodation.id = :p0", accommodationId);
  }

  public List<InvoiceItemEntity> findNonVoidByReservationRoomId(Long reservationRoomId) {
    positiveId(reservationRoomId);
    return select("e.reservationRoom.id = :p0 and e.invoice.status <> 'void'", reservationRoomId);
  }

  public List<InvoiceItemEntity> findNonVoidByServiceChargeId(Long serviceChargeId) {
    positiveId(serviceChargeId);
    return select("e.serviceCharge.id = :p0 and e.invoice.status <> 'void'", serviceChargeId);
  }

  public List<InvoiceItemEntity> findIssuedByReservationRoomId(Long p0) {
    return select("e.reservationRoom.id = :p0 and e.invoice.status = 'issued'", p0);
  }

  public List<InvoiceItemEntity> findIssuedByServiceChargeId(Long p0) {
    return select("e.serviceCharge.id = :p0 and e.invoice.status = 'issued'", p0);
  }
}
