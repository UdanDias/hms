package com.hms.service.impl;

import static com.hms.util.MoneyUtil.money;

import com.hms.dao.HotelDAO;
import com.hms.dao.InvoiceDAO;
import com.hms.dao.PaymentDAO;
import com.hms.dao.RefundDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dto.PaymentDTO;
import com.hms.entities.HotelEntity;
import com.hms.entities.InvoiceEntity;
import com.hms.entities.PaymentEntity;
import com.hms.entities.RefundEntity;
import com.hms.entities.ReservationEntity;
import com.hms.exception.HotelNotFoundException;
import com.hms.exception.InvoiceNotFoundException;
import com.hms.exception.PaymentNotFoundException;
import com.hms.exception.ReservationNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {
  private final PaymentDAO paymentDao;
  private final RefundDAO refundDao;
  private final InvoiceDAO invoiceDao;
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public PaymentDTO getSelectedPayment(Long paymentId) {
    serviceSecurity.getCurrentUser();
    PaymentEntity paymentEntity =
        paymentDao
            .findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    serviceSecurity.checkRead(paymentEntity);
    return entityDTOConvert.convertPaymentEntityToPaymentDTO(paymentEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDTO> getAllPayments() {
    serviceSecurity.getCurrentUser();
    List<PaymentEntity> entities =
        paymentDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertPaymentEntityListToPaymentDTOList(entities);
  }

  @Override
  public PaymentDTO addPayment(PaymentDTO paymentDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    PaymentEntity paymentEntity = entityDTOConvert.convertPaymentDTOToPaymentEntity(paymentDTO);
    if (paymentDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(paymentDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      paymentEntity.setHotel(hotel);
    }
    if (paymentDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(paymentDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      paymentEntity.setReservation(reservation);
    }
    if (paymentDTO.getInvoiceId() != null) {
      InvoiceEntity invoice =
          invoiceDao
              .findById(paymentDTO.getInvoiceId())
              .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
      paymentEntity.setInvoice(invoice);
    }

    validatePaymentForSave(paymentEntity, null);
    validationUtil.validate(paymentEntity);
    paymentDao.save(paymentEntity);
    auditRecorder.recordAction("CREATE", "Payment", paymentEntity.getId());
    paymentDao.flush();
    return entityDTOConvert.convertPaymentEntityToPaymentDTO(paymentEntity);
  }

  private void validatePaymentForSave(PaymentEntity paymentEntity, PaymentEntity existingPayment) {
    validationUtil.require(
        paymentEntity.getReservation() != null && paymentEntity.getHotel() != null,
        "Reservation and hotel are required");
    validationUtil.same(
        paymentEntity.getReservation().getHotel().getId(),
        paymentEntity.getHotel().getId(),
        "Payment");
    validationUtil.require(
        !Set.of("cancelled", "checked_out").contains(paymentEntity.getReservation().getStatus()),
        "Cannot record new payments for this reservation state");
    validationUtil.text(paymentEntity.getReferenceNumber(), "Unique payment reference/request ID");
    validationUtil.require(
        paymentDao
            .findByHotelIdAndReferenceNumber(
                paymentEntity.getHotel().getId(), paymentEntity.getReferenceNumber())
            .isEmpty(),
        "Payment reference already recorded; do not retry with a new reference");
    paymentEntity.setAmount(MoneyUtil.positive(paymentEntity.getAmount()));
    paymentEntity.setCurrencyCode(paymentEntity.getReservation().getCurrencyCode());
    validatePaymentMethod(paymentEntity.getPaymentMethod());
    if (paymentEntity.getInvoice() != null) {
      validationUtil.same(
          paymentEntity.getInvoice().getReservation().getId(),
          paymentEntity.getReservation().getId(),
          "Invoice");
      validationUtil.require(
          "issued".equals(paymentEntity.getInvoice().getStatus()),
          "Payment requires an issued invoice");
      validationUtil.require(
          paymentEntity
                  .getAmount()
                  .compareTo(calculateOutstandingBalance(paymentEntity.getInvoice()))
              <= 0,
          "Payment exceeds invoice balance");
    }
    paymentEntity.setReceiptNumber(UtilData.generateNumber("PAY"));
    paymentEntity.setStatus("completed");
    paymentEntity.setPaidAt(UtilData.generateNow());
    paymentEntity.setReceivedBy(serviceSecurity.getCurrentUser());
  }

  @Override
  public PaymentDTO allocatePayment(Long paymentId, Long invoiceId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    PaymentEntity paymentEntity =
        paymentDao
            .findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    allocateDepositToInvoice(
        paymentEntity,
        invoiceDao
            .findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found")));
    validationUtil.validate(paymentEntity);
    auditRecorder.recordAction("ALLOCATE", "Payment", paymentEntity.getId());
    paymentDao.flush();
    return entityDTOConvert.convertPaymentEntityToPaymentDTO(paymentEntity);
  }

  private BigDecimal calculateOutstandingBalance(InvoiceEntity i) {
    return money(i.getGrandTotal().subtract(calculatePaidAmount(i)));
  }

  private void validatePaymentMethod(String method) {
    validationUtil.require(
        method != null
            && Set.of("cash", "card", "bank_transfer", "cheque", "other").contains(method),
        "Unsupported manual payment/refund method");
  }

  private void allocateDepositToInvoice(PaymentEntity p, InvoiceEntity i) {
    validationUtil.require(p.getInvoice() == null, "Payment is already allocated");
    validationUtil.same(p.getReservation().getId(), i.getReservation().getId(), "Invoice");
    validationUtil.require("issued".equals(i.getStatus()), "Invoice must be issued");
    validationUtil.require(
        calculateNetPayment(p).signum() > 0
            && calculateNetPayment(p).compareTo(calculateOutstandingBalance(i)) <= 0,
        "Deposit does not fit the invoice's outstanding balance");
    p.setInvoice(i);
  }

  private BigDecimal calculateNetPayment(PaymentEntity p) {
    return p.getAmount().subtract(calculateRefundedAmount(p));
  }

  private BigDecimal calculatePaidAmount(InvoiceEntity i) {
    return paymentDao.findCompletedByInvoiceId(i.getId()).stream()
        .map(this::calculateNetPayment)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateRefundedAmount(PaymentEntity p) {
    return refundDao.findCompletedByPaymentId(p.getId()).stream()
        .map(RefundEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
