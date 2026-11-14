package com.hms.dao;

import com.hms.dao.support.AbstractHibernateDAO;
import com.hms.entities.PaymentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Persistence access for PaymentEntity; business decisions are made by the service layer. */
@Repository
public class PaymentDAO extends AbstractHibernateDAO<PaymentEntity> {
  public PaymentDAO() {
    super(PaymentEntity.class, "id", PaymentEntity::getId);
  }

  public List<PaymentEntity> findByHotelId(Long hotelId) {
    positiveId(hotelId);
    return select("e.hotel.id = :p0", hotelId);
  }

  public List<PaymentEntity> findByReservationId(Long reservationId) {
    positiveId(reservationId);
    return select("e.reservation.id = :p0", reservationId);
  }

  public List<PaymentEntity> findByInvoiceId(Long invoiceId) {
    positiveId(invoiceId);
    return select("e.invoice.id = :p0", invoiceId);
  }

  public List<PaymentEntity> findByReceivedById(Long receivedById) {
    positiveId(receivedById);
    return select("e.receivedBy.userId = :p0", receivedById);
  }

  public Optional<PaymentEntity> findByReceiptNumber(String receiptNumber) {
    return selectOne("e.receiptNumber = :p0", receiptNumber);
  }

  public List<PaymentEntity> findCompletedByInvoiceId(Long invoiceId) {
    positiveId(invoiceId);
    return select("e.invoice.id = :p0 and e.status = 'completed'", invoiceId);
  }

  public List<PaymentEntity> findByHotelIdAndReferenceNumber(Long hotelId, String referenceNumber) {
    positiveId(hotelId);
    return select("e.hotel.id = :p0 and e.referenceNumber = :p1", hotelId, referenceNumber);
  }

  public List<PaymentEntity> findUnallocatedByReservationId(Long p0) {
    return select("e.reservation.id = :p0 and e.invoice is null", p0);
  }
}
