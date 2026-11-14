package com.hms.service.impl.support;

import static com.hms.service.impl.support.ServiceSupport.money;

import com.hms.dao.HmsDao;
import com.hms.entities.*;
import java.math.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillingRules {
  private final HmsDao dao;
  private final ServiceSupport s;

  public void draft(InvoiceEntity invoice) {
    s.require("draft".equals(invoice.getStatus()), "Only draft invoices may be edited");
  }

  public void prepareInvoice(InvoiceEntity e, InvoiceEntity old) {
    s.require(
        e.getReservation() != null && e.getHotel() != null && e.getBilledCustomer() != null,
        "Hotel, reservation and billed customer are required");
    s.same(e.getHotel().getId(), e.getReservation().getHotel().getId(), "Invoice");
    s.require(
        !"cancelled".equals(e.getReservation().getStatus()),
        "Cannot invoice a cancelled reservation");
    if (old == null) {
      e.setInvoiceNumber(s.number("INV"));
      e.setCreatedBy(s.actor());
      e.setStatus("draft");
      e.setIssuedAt(null);
      zeroInvoice(e);
    } else {
      draft(old);
      s.same(old.getReservation().getId(), e.getReservation().getId(), "Reservation");
      s.same(old.getHotel().getId(), e.getHotel().getId(), "Hotel");
      e.setInvoiceNumber(old.getInvoiceNumber());
      e.setCreatedBy(old.getCreatedBy());
      e.setStatus("draft");
      e.setIssuedAt(null);
      e.setSubtotal(old.getSubtotal());
      e.setDiscountTotal(old.getDiscountTotal());
      e.setTaxTotal(old.getTaxTotal());
      e.setServiceChargeTotal(old.getServiceChargeTotal());
      e.setGrandTotal(old.getGrandTotal());
    }
    e.setCurrencyCode(e.getReservation().getCurrencyCode());
    e.setBillingNameSnapshot(e.getBilledCustomer().getName());
    e.setBillingAddressSnapshot(Optional.ofNullable(e.getBilledCustomer().getAddress()).orElse(""));
  }

  private void zeroInvoice(InvoiceEntity e) {
    e.setSubtotal(money(null));
    e.setDiscountTotal(money(null));
    e.setTaxTotal(money(null));
    e.setServiceChargeTotal(money(null));
    e.setGrandTotal(money(null));
  }

  public void prepareItem(InvoiceItemEntity e, InvoiceItemEntity old) {
    s.require(e.getInvoice() != null, "Invoice is required");
    draft(e.getInvoice());
    if (old != null) {
      draft(old.getInvoice());
      s.same(e.getInvoice().getId(), old.getInvoice().getId(), "Invoice");
    }
    s.require(
        e.getReservationRoom() == null || e.getServiceCharge() == null,
        "An item cannot bill both a room and a service");
    if (e.getReservationRoom() != null) {
      var room = e.getReservationRoom();
      s.same(
          room.getReservation().getId(),
          e.getInvoice().getReservation().getId(),
          "Room reservation");
      s.require(
          !"pending".equals(room.getStatus()) && !"cancelled".equals(room.getStatus()),
          "Confirm the reservation before billing a room");
      uniqueSource("e.reservationRoom.id", room.getId(), old);
      e.setQuantity(BigDecimal.ONE);
      e.setUnitPrice(
          money(
              room.getTotalAmount()
                  .subtract(room.getTaxAmount())
                  .subtract(room.getServiceChargeAmount())
                  .add(room.getDiscountAmount())));
      e.setDiscountAmount(room.getDiscountAmount());
      e.setTaxRate(BigDecimal.ZERO);
      e.setTaxAmount(room.getTaxAmount());
      e.setServiceChargeAmount(room.getServiceChargeAmount());
      e.setAccommodation(room.getAccommodation());
      e.setDescription(
          "Stay "
              + room.getCheckInDate()
              + " to "
              + room.getCheckOutDate()
              + " — "
              + room.getAccommodation().getType().getValue());
    } else if (e.getServiceCharge() != null) {
      var charge = e.getServiceCharge();
      s.same(
          charge.getReservation().getId(),
          e.getInvoice().getReservation().getId(),
          "Service charge");
      s.require("active".equals(charge.getStatus()), "Only active service charges can be billed");
      uniqueSource("e.serviceCharge.id", charge.getId(), old);
      e.setQuantity(charge.getQuantity());
      e.setUnitPrice(charge.getUnitPrice());
      e.setDiscountAmount(charge.getDiscountAmount());
      e.setTaxAmount(charge.getTaxAmount());
      e.setTaxRate(BigDecimal.ZERO);
      e.setServiceChargeAmount(BigDecimal.ZERO);
      e.setAccommodation(null);
      e.setDescription(charge.getDescriptionSnapshot());
    } else {
      s.require(
          e.getAccommodation() == null,
          "Accommodation is included through a room line, not billed independently");
      s.text(e.getDescription(), "Item description");
      e.setQuantity(quantity(e.getQuantity()));
      e.setUnitPrice(s.nonnegative(e.getUnitPrice()));
      e.setDiscountAmount(s.nonnegative(e.getDiscountAmount()));
      var base = money(e.getQuantity().multiply(e.getUnitPrice()));
      s.require(e.getDiscountAmount().compareTo(base) <= 0, "Discount exceeds line amount");
      e.setTaxRate(taxRate(e.getTaxRate()));
      e.setTaxAmount(
          money(
              base.subtract(e.getDiscountAmount())
                  .multiply(e.getTaxRate())
                  .divide(BigDecimal.valueOf(100))));
      e.setServiceChargeAmount(s.nonnegative(e.getServiceChargeAmount()));
    }
    e.setLineTotal(
        money(
            e.getQuantity()
                .multiply(e.getUnitPrice())
                .subtract(e.getDiscountAmount())
                .add(e.getTaxAmount())
                .add(e.getServiceChargeAmount())));
  }

  private void uniqueSource(String path, Long id, InvoiceItemEntity old) {
    s.require(
        dao
            .query(InvoiceItemEntity.class, path + " = :p0 and e.invoice.status <> 'void'", id)
            .stream()
            .noneMatch(x -> old == null || !x.getId().equals(old.getId())),
        "This source is already billed on a non-void invoice");
  }

  public void recalculate(InvoiceEntity invoice) {
    dao.flush();
    zeroInvoice(invoice);
    for (var item : dao.query(InvoiceItemEntity.class, "e.invoice.id = :p0", invoice.getId())) {
      invoice.setSubtotal(
          invoice.getSubtotal().add(money(item.getQuantity().multiply(item.getUnitPrice()))));
      invoice.setDiscountTotal(invoice.getDiscountTotal().add(item.getDiscountAmount()));
      invoice.setTaxTotal(invoice.getTaxTotal().add(item.getTaxAmount()));
      invoice.setServiceChargeTotal(
          invoice.getServiceChargeTotal().add(item.getServiceChargeAmount()));
      invoice.setGrandTotal(invoice.getGrandTotal().add(item.getLineTotal()));
    }
  }

  public void issue(InvoiceEntity invoice) {
    draft(invoice);
    s.require(
        !dao.query(InvoiceItemEntity.class, "e.invoice.id = :p0", invoice.getId()).isEmpty(),
        "Cannot issue an empty invoice");
    s.require(
        Set.of("confirmed", "checked_in", "checked_out")
            .contains(invoice.getReservation().getStatus()),
        "Reservation must be confirmed before issuing an invoice");
    recalculate(invoice);
    invoice.setStatus("issued");
    invoice.setIssuedAt(s.now());
  }

  public BigDecimal refunded(PaymentEntity p) {
    return dao
        .query(RefundEntity.class, "e.payment.id = :p0 and e.status = 'completed'", p.getId())
        .stream()
        .map(RefundEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal net(PaymentEntity p) {
    return p.getAmount().subtract(refunded(p));
  }

  public BigDecimal paid(InvoiceEntity i) {
    return dao
        .query(PaymentEntity.class, "e.invoice.id = :p0 and e.status = 'completed'", i.getId())
        .stream()
        .map(this::net)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal balance(InvoiceEntity i) {
    return money(i.getGrandTotal().subtract(paid(i)));
  }

  public void voidInvoice(InvoiceEntity i) {
    s.require(Set.of("draft", "issued").contains(i.getStatus()), "Invoice is already void");
    s.require(paid(i).signum() == 0, "Refund all allocated payments before voiding an invoice");
    s.require(
        !"checked_out".equals(i.getReservation().getStatus()),
        "Cannot void invoices after checkout without a credit-note workflow");
    i.setStatus("void");
  }

  public void preparePayment(PaymentEntity e) {
    s.require(
        e.getReservation() != null && e.getHotel() != null, "Reservation and hotel are required");
    s.same(e.getReservation().getHotel().getId(), e.getHotel().getId(), "Payment");
    s.require(
        !Set.of("cancelled", "checked_out").contains(e.getReservation().getStatus()),
        "Cannot record new payments for this reservation state");
    s.text(e.getReferenceNumber(), "Unique payment reference/request ID");
    s.require(
        dao.query(
                PaymentEntity.class,
                "e.hotel.id = :p0 and e.referenceNumber = :p1",
                e.getHotel().getId(),
                e.getReferenceNumber())
            .isEmpty(),
        "Payment reference already recorded; do not retry with a new reference");
    e.setAmount(s.positive(e.getAmount()));
    e.setCurrencyCode(e.getReservation().getCurrencyCode());
    method(e.getPaymentMethod());
    if (e.getInvoice() != null) {
      s.same(e.getInvoice().getReservation().getId(), e.getReservation().getId(), "Invoice");
      s.require("issued".equals(e.getInvoice().getStatus()), "Payment requires an issued invoice");
      s.require(
          e.getAmount().compareTo(balance(e.getInvoice())) <= 0, "Payment exceeds invoice balance");
    }
    e.setReceiptNumber(s.number("PAY"));
    e.setStatus("completed");
    e.setPaidAt(s.now());
    e.setReceivedBy(s.actor());
  }

  private void method(String method) {
    s.require(
        method != null
            && Set.of("cash", "card", "bank_transfer", "cheque", "other").contains(method),
        "Unsupported manual payment/refund method");
  }

  public void prepareRefund(RefundEntity e) {
    s.require(
        e.getPayment() != null && "completed".equals(e.getPayment().getStatus()),
        "Completed payment is required");
    s.require(
        !"checked_out".equals(e.getPayment().getReservation().getStatus()),
        "Post-checkout adjustments require a credit-note policy");
    s.text(e.getReferenceNumber(), "Unique refund reference/request ID");
    s.require(
        dao.query(
                RefundEntity.class,
                "e.payment.id = :p0 and e.referenceNumber = :p1",
                e.getPayment().getId(),
                e.getReferenceNumber())
            .isEmpty(),
        "Refund reference already recorded; do not retry with a new reference");
    e.setAmount(s.positive(e.getAmount()));
    s.require(
        e.getAmount().compareTo(net(e.getPayment())) <= 0,
        "Refund exceeds unrefunded payment amount");
    s.text(e.getReason(), "Refund reason");
    method(e.getRefundMethod());
    e.setRefundNumber(s.number("REF"));
    e.setStatus("completed");
    e.setRefundedAt(s.now());
    e.setProcessedBy(s.actor());
  }

  public void allocate(PaymentEntity p, InvoiceEntity i) {
    s.require(p.getInvoice() == null, "Payment is already allocated");
    s.same(p.getReservation().getId(), i.getReservation().getId(), "Invoice");
    s.require("issued".equals(i.getStatus()), "Invoice must be issued");
    s.require(
        net(p).signum() > 0 && net(p).compareTo(balance(i)) <= 0,
        "Deposit does not fit the invoice's outstanding balance");
    p.setInvoice(i);
  }

  public BigDecimal quantity(BigDecimal value) {
    s.require(
        value != null && value.signum() > 0 && value.scale() <= 3,
        "Quantity must be positive with at most 3 decimal places");
    return value;
  }

  public BigDecimal taxRate(BigDecimal value) {
    var x = value == null ? BigDecimal.ZERO : value;
    s.require(
        x.signum() >= 0 && x.compareTo(BigDecimal.valueOf(100)) <= 0,
        "Tax percentage must be between 0 and 100");
    return x;
  }

  public void unbilled(ServiceChargeEntity charge) {
    s.require(
        dao.query(
                InvoiceItemEntity.class,
                "e.serviceCharge.id = :p0 and e.invoice.status <> 'void'",
                charge.getId())
            .isEmpty(),
        "Service charge is already billed");
  }

  public void prepareCharge(ServiceChargeEntity e, ServiceChargeEntity old) {
    s.require(
        e.getReservation() != null && e.getService() != null,
        "Reservation and service are required");
    s.same(e.getReservation().getHotel().getId(), e.getService().getHotel().getId(), "Service");
    s.require(
        Set.of("confirmed", "checked_in").contains(e.getReservation().getStatus()),
        "Service charges require a confirmed or checked-in reservation");
    s.require(Boolean.TRUE.equals(e.getService().getIsActive()), "Service is inactive");
    if (e.getReservationRoom() != null)
      s.same(
          e.getReservationRoom().getReservation().getId(),
          e.getReservation().getId(),
          "Room reservation");
    if (old != null) {
      unbilled(old);
      s.require("active".equals(old.getStatus()), "Voided charges cannot be edited");
      s.same(old.getReservation().getId(), e.getReservation().getId(), "Reservation");
    }
    e.setQuantity(quantity(e.getQuantity()));
    e.setUnitPrice(
        s.nonnegative(
            e.getUnitPrice() == null ? e.getService().getDefaultPrice() : e.getUnitPrice()));
    e.setDiscountAmount(s.nonnegative(e.getDiscountAmount()));
    var base = money(e.getQuantity().multiply(e.getUnitPrice()));
    s.require(e.getDiscountAmount().compareTo(base) <= 0, "Discount exceeds service amount");
    e.setTaxAmount(
        money(
            base.subtract(e.getDiscountAmount())
                .multiply(taxRate(e.getService().getDefaultTaxRate()))
                .divide(BigDecimal.valueOf(100))));
    e.setTotalAmount(money(base.subtract(e.getDiscountAmount()).add(e.getTaxAmount())));
    e.setDescriptionSnapshot(e.getService().getName());
    e.setStatus("active");
    e.setProvidedAt(old == null ? s.now() : old.getProvidedAt());
    e.setRecordedBy(old == null ? s.actor() : old.getRecordedBy());
  }

  public void settledForCheckout(ReservationEntity r) {
    var invoices =
        dao.query(InvoiceEntity.class, "e.reservation.id = :p0 and e.status <> 'void'", r.getId());
    s.require(!invoices.isEmpty(), "An issued invoice is required before checkout");
    for (var i : invoices)
      s.require(
          "issued".equals(i.getStatus()) && balance(i).signum() == 0,
          "All invoices must be issued and settled before checkout");
    for (var room :
        dao.query(
            ReservationRoomEntity.class,
            "e.reservation.id = :p0 and e.status <> 'cancelled'",
            r.getId()))
      s.require(
          !dao.query(
                  InvoiceItemEntity.class,
                  "e.reservationRoom.id = :p0 and e.invoice.status = 'issued'",
                  room.getId())
              .isEmpty(),
          "Every room must be invoiced before checkout");
    for (var c :
        dao.query(
            ServiceChargeEntity.class, "e.reservation.id = :p0 and e.status = 'active'", r.getId()))
      s.require(
          !dao.query(
                  InvoiceItemEntity.class,
                  "e.serviceCharge.id = :p0 and e.invoice.status = 'issued'",
                  c.getId())
              .isEmpty(),
          "Every active service charge must be invoiced before checkout");
    s.require(
        dao
            .query(PaymentEntity.class, "e.reservation.id = :p0 and e.invoice is null", r.getId())
            .stream()
            .allMatch(x -> net(x).signum() == 0),
        "Allocate or refund unapplied deposits before checkout");
  }
}
