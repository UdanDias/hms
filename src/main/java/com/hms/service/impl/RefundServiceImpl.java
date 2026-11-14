package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.PaymentDAO;
import com.hms.dao.RefundDAO;
import com.hms.dto.RefundDTO;
import com.hms.entities.PaymentEntity;
import com.hms.entities.RefundEntity;
import com.hms.exception.PaymentNotFoundException;
import com.hms.exception.RefundNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.RefundService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.MoneyUtil;
import com.hms.util.UtilData;
import com.hms.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class RefundServiceImpl implements RefundService {
  private final PaymentDAO paymentDao;
  private final RefundDAO refundDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public RefundDTO getSelectedRefund(Long refundId) {
    serviceSecurity.getCurrentUser();
    RefundEntity refundEntity =
        refundDao
            .findById(refundId)
            .orElseThrow(() -> new RefundNotFoundException("Refund not found"));
    serviceSecurity.checkRead(refundEntity);
    return entityDTOConvert.convertRefundEntityToRefundDTO(refundEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RefundDTO> getAllRefunds() {
    serviceSecurity.getCurrentUser();
    List<RefundEntity> entities =
        refundDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertRefundEntityListToRefundDTOList(entities);
  }

  @Override
  public RefundDTO addRefund(RefundDTO refundDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    RefundEntity refundEntity = entityDTOConvert.convertRefundDTOToRefundEntity(refundDTO);
    if (refundDTO.getPaymentId() != null) {
      PaymentEntity payment =
          paymentDao
              .findById(refundDTO.getPaymentId())
              .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
      refundEntity.setPayment(payment);
    }

    validateRefundForSave(refundEntity, null);
    validationUtil.validate(refundEntity);
    refundDao.save(refundEntity);
    auditRecorder.recordAction("CREATE", "Refund", refundEntity.getId());
    refundDao.flush();
    return entityDTOConvert.convertRefundEntityToRefundDTO(refundEntity);
  }

  private void validateRefundForSave(RefundEntity refundEntity, RefundEntity existingRefund) {
    validationUtil.require(
        refundEntity.getPayment() != null
            && "completed".equals(refundEntity.getPayment().getStatus()),
        "Completed payment is required");
    validationUtil.require(
        !"checked_out".equals(refundEntity.getPayment().getReservation().getStatus()),
        "Post-checkout adjustments require a credit-note policy");
    validationUtil.text(refundEntity.getReferenceNumber(), "Unique refund reference/request ID");
    validationUtil.require(
        refundDao
            .findByPaymentIdAndReferenceNumber(
                refundEntity.getPayment().getId(), refundEntity.getReferenceNumber())
            .isEmpty(),
        "Refund reference already recorded; do not retry with a new reference");
    refundEntity.setAmount(MoneyUtil.positive(refundEntity.getAmount()));
    validationUtil.require(
        refundEntity.getAmount().compareTo(calculateNetPayment(refundEntity.getPayment())) <= 0,
        "Refund exceeds unrefunded payment amount");
    validationUtil.text(refundEntity.getReason(), "Refund reason");
    validatePaymentMethod(refundEntity.getRefundMethod());
    refundEntity.setRefundNumber(UtilData.generateNumber("REF"));
    refundEntity.setStatus("completed");
    refundEntity.setRefundedAt(UtilData.generateNow());
    refundEntity.setProcessedBy(serviceSecurity.getCurrentUser());
  }

  private BigDecimal calculateNetPayment(PaymentEntity p) {
    return p.getAmount().subtract(calculateRefundedAmount(p));
  }

  private void validatePaymentMethod(String method) {
    validationUtil.require(
        method != null
            && Set.of("cash", "card", "bank_transfer", "cheque", "other").contains(method),
        "Unsupported manual payment/refund method");
  }

  private BigDecimal calculateRefundedAmount(PaymentEntity p) {
    return refundDao.findCompletedByPaymentId(p.getId()).stream()
        .map(RefundEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
