package com.hms.service.impl;

import static com.hms.util.MoneyUtil.money;

import com.hms.dao.CustomerDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.InvoiceDAO;
import com.hms.dao.InvoiceItemDAO;
import com.hms.dao.PaymentDAO;
import com.hms.dao.RefundDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dto.InvoiceDTO;
import com.hms.entities.CustomerEntity;
import com.hms.entities.HotelEntity;
import com.hms.entities.InvoiceEntity;
import com.hms.entities.PaymentEntity;
import com.hms.entities.RefundEntity;
import com.hms.entities.ReservationEntity;
import com.hms.exception.CustomerNotFoundException;
import com.hms.exception.HotelNotFoundException;
import com.hms.exception.InvoiceNotFoundException;
import com.hms.exception.ReservationNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.InvoiceService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.UtilData;
import com.hms.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class InvoiceServiceImpl implements InvoiceService {
  private final CustomerDAO customerDao;
  private final PaymentDAO paymentDao;
  private final RefundDAO refundDao;
  private final InvoiceDAO invoiceDao;
  private final InvoiceItemDAO invoiceItemDao;
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public InvoiceDTO getSelectedInvoice(Long invoiceId) {
    serviceSecurity.getCurrentUser();
    InvoiceEntity invoiceEntity =
        invoiceDao
            .findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    serviceSecurity.checkRead(invoiceEntity);
    return entityDTOConvert.convertInvoiceEntityToInvoiceDTO(invoiceEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvoiceDTO> getAllInvoices() {
    serviceSecurity.getCurrentUser();
    List<InvoiceEntity> entities =
        invoiceDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertInvoiceEntityListToInvoiceDTOList(entities);
  }

  @Override
  public InvoiceDTO addInvoice(InvoiceDTO invoiceDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceEntity invoiceEntity = entityDTOConvert.convertInvoiceDTOToInvoiceEntity(invoiceDTO);
    if (invoiceDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(invoiceDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      invoiceEntity.setHotel(hotel);
    }
    if (invoiceDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(invoiceDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      invoiceEntity.setReservation(reservation);
    }
    if (invoiceDTO.getBilledCustomerId() != null) {
      CustomerEntity billedCustomer =
          customerDao
              .findById(invoiceDTO.getBilledCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      invoiceEntity.setBilledCustomer(billedCustomer);
    }

    validateInvoiceForSave(invoiceEntity, null);
    validationUtil.validate(invoiceEntity);
    invoiceDao.save(invoiceEntity);
    auditRecorder.recordAction("CREATE", "Invoice", invoiceEntity.getId());
    invoiceDao.flush();
    return entityDTOConvert.convertInvoiceEntityToInvoiceDTO(invoiceEntity);
  }

  @Override
  public InvoiceDTO updateInvoice(Long invoiceId, InvoiceDTO invoiceDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceEntity invoiceEntity =
        invoiceDao
            .findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    InvoiceEntity requestedInvoice = entityDTOConvert.convertInvoiceDTOToInvoiceEntity(invoiceDTO);
    if (invoiceDTO.getHotelId() != null) {
      HotelEntity hotel =
          hotelDao
              .findById(invoiceDTO.getHotelId())
              .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
      requestedInvoice.setHotel(hotel);
    }
    if (invoiceDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(invoiceDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      requestedInvoice.setReservation(reservation);
    }
    if (invoiceDTO.getBilledCustomerId() != null) {
      CustomerEntity billedCustomer =
          customerDao
              .findById(invoiceDTO.getBilledCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      requestedInvoice.setBilledCustomer(billedCustomer);
    }

    validateInvoiceForSave(requestedInvoice, invoiceEntity);
    validationUtil.validate(requestedInvoice);
    invoiceEntity.setHotel(requestedInvoice.getHotel());
    invoiceEntity.setReservation(requestedInvoice.getReservation());
    invoiceEntity.setInvoiceNumber(requestedInvoice.getInvoiceNumber());
    invoiceEntity.setBilledCustomer(requestedInvoice.getBilledCustomer());
    invoiceEntity.setBillingNameSnapshot(requestedInvoice.getBillingNameSnapshot());
    invoiceEntity.setBillingAddressSnapshot(requestedInvoice.getBillingAddressSnapshot());
    invoiceEntity.setCurrencyCode(requestedInvoice.getCurrencyCode());
    invoiceEntity.setIssuedAt(requestedInvoice.getIssuedAt());
    invoiceEntity.setDueDate(requestedInvoice.getDueDate());
    invoiceEntity.setSubtotal(requestedInvoice.getSubtotal());
    invoiceEntity.setDiscountTotal(requestedInvoice.getDiscountTotal());
    invoiceEntity.setTaxTotal(requestedInvoice.getTaxTotal());
    invoiceEntity.setServiceChargeTotal(requestedInvoice.getServiceChargeTotal());
    invoiceEntity.setGrandTotal(requestedInvoice.getGrandTotal());
    invoiceEntity.setStatus(requestedInvoice.getStatus());
    invoiceEntity.setCreatedBy(requestedInvoice.getCreatedBy());
    validationUtil.validate(invoiceEntity);
    invoiceDao.save(invoiceEntity);
    auditRecorder.recordAction("UPDATE", "Invoice", invoiceEntity.getId());
    invoiceDao.flush();
    return entityDTOConvert.convertInvoiceEntityToInvoiceDTO(invoiceEntity);
  }

  private void validateInvoiceForSave(InvoiceEntity invoiceEntity, InvoiceEntity existingInvoice) {
    validationUtil.require(
        invoiceEntity.getReservation() != null
            && invoiceEntity.getHotel() != null
            && invoiceEntity.getBilledCustomer() != null,
        "Hotel, reservation and billed customer are required");
    validationUtil.same(
        invoiceEntity.getHotel().getId(),
        invoiceEntity.getReservation().getHotel().getId(),
        "Invoice");
    validationUtil.require(
        !"cancelled".equals(invoiceEntity.getReservation().getStatus()),
        "Cannot invoice a cancelled reservation");
    if (existingInvoice == null) {
      invoiceEntity.setInvoiceNumber(UtilData.generateNumber("INV"));
      invoiceEntity.setCreatedBy(serviceSecurity.getCurrentUser());
      invoiceEntity.setStatus("draft");
      invoiceEntity.setIssuedAt(null);
      resetInvoiceTotals(invoiceEntity);
    } else {
      requireDraftInvoice(existingInvoice);
      validationUtil.same(
          existingInvoice.getReservation().getId(),
          invoiceEntity.getReservation().getId(),
          "Reservation");
      validationUtil.same(
          existingInvoice.getHotel().getId(), invoiceEntity.getHotel().getId(), "Hotel");
      invoiceEntity.setInvoiceNumber(existingInvoice.getInvoiceNumber());
      invoiceEntity.setCreatedBy(existingInvoice.getCreatedBy());
      invoiceEntity.setStatus("draft");
      invoiceEntity.setIssuedAt(null);
      invoiceEntity.setSubtotal(existingInvoice.getSubtotal());
      invoiceEntity.setDiscountTotal(existingInvoice.getDiscountTotal());
      invoiceEntity.setTaxTotal(existingInvoice.getTaxTotal());
      invoiceEntity.setServiceChargeTotal(existingInvoice.getServiceChargeTotal());
      invoiceEntity.setGrandTotal(existingInvoice.getGrandTotal());
    }
    invoiceEntity.setCurrencyCode(invoiceEntity.getReservation().getCurrencyCode());
    invoiceEntity.setBillingNameSnapshot(invoiceEntity.getBilledCustomer().getName());
    invoiceEntity.setBillingAddressSnapshot(
        Optional.ofNullable(invoiceEntity.getBilledCustomer().getAddress()).orElse(""));
  }

  @Override
  public InvoiceDTO issueInvoice(Long invoiceId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceEntity invoiceEntity =
        invoiceDao
            .findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    issueDraftInvoice(invoiceEntity);
    validationUtil.validate(invoiceEntity);
    auditRecorder.recordAction("ISSUE", "Invoice", invoiceEntity.getId());
    invoiceDao.flush();
    return entityDTOConvert.convertInvoiceEntityToInvoiceDTO(invoiceEntity);
  }

  @Override
  public InvoiceDTO voidInvoice(Long invoiceId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceEntity invoiceEntity =
        invoiceDao
            .findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    voidUnpaidInvoice(invoiceEntity);
    validationUtil.validate(invoiceEntity);
    auditRecorder.recordAction("VOID", "Invoice", invoiceEntity.getId());
    invoiceDao.flush();
    return entityDTOConvert.convertInvoiceEntityToInvoiceDTO(invoiceEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getOutstandingBalance(Long invoiceId) {
    serviceSecurity.getCurrentUser();
    InvoiceEntity invoiceEntity =
        invoiceDao
            .findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    serviceSecurity.checkRead(invoiceEntity);
    return calculateOutstandingBalance(invoiceEntity);
  }

  private void requireDraftInvoice(InvoiceEntity invoice) {
    validationUtil.require(
        "draft".equals(invoice.getStatus()), "Only draft invoices may be edited");
  }

  private void resetInvoiceTotals(InvoiceEntity e) {
    e.setSubtotal(money(null));
    e.setDiscountTotal(money(null));
    e.setTaxTotal(money(null));
    e.setServiceChargeTotal(money(null));
    e.setGrandTotal(money(null));
  }

  private void issueDraftInvoice(InvoiceEntity invoice) {
    requireDraftInvoice(invoice);
    validationUtil.require(
        !invoiceItemDao.findByInvoiceId(invoice.getId()).isEmpty(),
        "Cannot issue an empty invoice");
    validationUtil.require(
        Set.of("confirmed", "checked_in", "checked_out")
            .contains(invoice.getReservation().getStatus()),
        "Reservation must be confirmed before issuing an invoice");
    recalculateInvoiceTotals(invoice);
    invoice.setStatus("issued");
    invoice.setIssuedAt(UtilData.generateNow());
  }

  private BigDecimal calculateOutstandingBalance(InvoiceEntity i) {
    return money(i.getGrandTotal().subtract(calculatePaidAmount(i)));
  }

  private void voidUnpaidInvoice(InvoiceEntity i) {
    validationUtil.require(
        Set.of("draft", "issued").contains(i.getStatus()), "Invoice is already void");
    validationUtil.require(
        calculatePaidAmount(i).signum() == 0,
        "Refund all allocated payments before voiding an invoice");
    validationUtil.require(
        !"checked_out".equals(i.getReservation().getStatus()),
        "Cannot void invoices after checkout without a credit-note workflow");
    i.setStatus("void");
  }

  private void recalculateInvoiceTotals(InvoiceEntity invoice) {
    invoiceDao.flush();
    resetInvoiceTotals(invoice);
    for (var item : invoiceItemDao.findByInvoiceId(invoice.getId())) {
      invoice.setSubtotal(
          invoice.getSubtotal().add(money(item.getQuantity().multiply(item.getUnitPrice()))));
      invoice.setDiscountTotal(invoice.getDiscountTotal().add(item.getDiscountAmount()));
      invoice.setTaxTotal(invoice.getTaxTotal().add(item.getTaxAmount()));
      invoice.setServiceChargeTotal(
          invoice.getServiceChargeTotal().add(item.getServiceChargeAmount()));
      invoice.setGrandTotal(invoice.getGrandTotal().add(item.getLineTotal()));
    }
  }

  private BigDecimal calculatePaidAmount(InvoiceEntity i) {
    return paymentDao.findCompletedByInvoiceId(i.getId()).stream()
        .map(this::calculateNetPayment)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateNetPayment(PaymentEntity p) {
    return p.getAmount().subtract(calculateRefundedAmount(p));
  }

  private BigDecimal calculateRefundedAmount(PaymentEntity p) {
    return refundDao.findCompletedByPaymentId(p.getId()).stream()
        .map(RefundEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
