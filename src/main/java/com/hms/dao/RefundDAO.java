package com.hms.dao;

import com.hms.dao.support.AbstractHibernateDAO;
import com.hms.entities.RefundEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Persistence access for RefundEntity; business decisions are made by the service layer. */
@Repository
public class RefundDAO extends AbstractHibernateDAO<RefundEntity> {
  public RefundDAO() {
    super(RefundEntity.class, "id", RefundEntity::getId);
  }

  public List<RefundEntity> findByPaymentId(Long paymentId) {
    positiveId(paymentId);
    return select("e.payment.id = :p0", paymentId);
  }

  public List<RefundEntity> findByProcessedById(Long processedById) {
    positiveId(processedById);
    return select("e.processedBy.userId = :p0", processedById);
  }

  public Optional<RefundEntity> findByRefundNumber(String refundNumber) {
    return selectOne("e.refundNumber = :p0", refundNumber);
  }

  public BigDecimal sumCompletedByPaymentId(Long paymentId) {
    positiveId(paymentId);
    var total =
        entityManager
            .createQuery(
                "select sum(e.amount) from RefundEntity e where e.payment.id = :paymentId and"
                    + " e.status = 'completed'",
                BigDecimal.class)
            .setParameter("paymentId", paymentId)
            .getSingleResult();
    return total == null ? BigDecimal.ZERO : total;
  }

  public List<RefundEntity> findCompletedByPaymentId(Long p0) {
    return select("e.payment.id = :p0 and e.status = 'completed'", p0);
  }

  public List<RefundEntity> findByPaymentIdAndReferenceNumber(Long p0, String p1) {
    return select("e.payment.id = :p0 and e.referenceNumber = :p1", p0, p1);
  }
}
