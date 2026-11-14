package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.PaymentDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * HMS business service for Payment.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {
    private final PaymentDao paymentDao;
    private final HotelDao hotelDao;
    private final ReservationDao reservationDao;
    private final InvoiceDao invoiceDao;
    private final UserDao userDao;

    private Payment getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Payment ID");
        return paymentDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    private PaymentDTO toDto(Payment entity) {
        PaymentDTO dto = new PaymentDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        dto.setReservationId(entity.getReservation() == null ? null : entity.getReservation().getId());
        dto.setInvoiceId(entity.getInvoice() == null ? null : entity.getInvoice().getId());
        dto.setReceivedById(entity.getReceivedBy() == null ? null : entity.getReceivedBy().getUserId());
        return dto;
    }

    private void apply(PaymentDTO dto, Payment entity) {
        if (dto.getReceiptNumber() != null) entity.setReceiptNumber(dto.getReceiptNumber());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
        if (dto.getCurrencyCode() != null) entity.setCurrencyCode(dto.getCurrencyCode());
        if (dto.getPaymentMethod() != null) entity.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getReferenceNumber() != null) entity.setReferenceNumber(dto.getReferenceNumber());
        // NEVER take settlement status or paid-at timestamp from client DTO.
        // A trusted provider callback/cashier approval workflow must post settlement.
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
        if (dto.getReservationId() != null) entity.setReservation(
                    reservationDao.findById(dto.getReservationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId())));
        if (dto.getInvoiceId() != null) entity.setInvoice(
                    invoiceDao.findById(dto.getInvoiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + dto.getInvoiceId())));
        if (dto.getReceivedById() != null) entity.setReceivedBy(
                    userDao.findById(dto.getReceivedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getReceivedById())));
    }

    @Override
    public PaymentDTO getSelectedPayment(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return paymentDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public PaymentDTO addPayment(PaymentDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Payment DTO is required");
        if (dto.getReceiptNumber() == null || dto.getReceiptNumber().isBlank()) throw new IllegalArgumentException("Receipt number is required");
        Payment entity = new Payment();
        apply(dto, entity);
        entity.setStatus("PENDING");
        // Existing payment schema requires paidAt even for PENDING; revise schema to
        // make it nullable and set the timestamp only after settlement is verified.
        entity.setPaidAt(LocalDateTime.now(ZoneOffset.UTC));
        validatePayment(entity);
        entity = paymentDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Payment DTO is required");
        Payment entity = getEntity(id);
        if (isPosted(entity.getStatus())) throw new IllegalStateException("Posted payments are immutable; use a refund or reversal");
        apply(dto, entity);
        validatePayment(entity);
        entity = paymentDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        Payment entity = getEntity(id);
        if (isPosted(entity.getStatus())) throw new IllegalStateException("Posted payments cannot be deleted; use a reversal");
        paymentDao.delete(entity);
        paymentDao.flush();
        // Nothing extra.
    }


    private static final Set<String> SETTLED = Set.of("COMPLETED", "PAID", "SUCCESS", "SETTLED");
    private static String upper(String s) {return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);}
    private static boolean isPosted(String status) {return SETTLED.contains(upper(status));}

    private void validatePayment(Payment payment) {
        if (payment.getHotel() == null || payment.getReservation() == null)
            throw new IllegalArgumentException("Payment hotel and reservation are required");
        if (!payment.getHotel().getId().equals(payment.getReservation().getHotel().getId()))
            throw new IllegalArgumentException("Payment hotel must match the reservation hotel");
        if (payment.getCurrencyCode() == null || payment.getReservation().getCurrencyCode() == null ||
            !payment.getCurrencyCode().equalsIgnoreCase(payment.getReservation().getCurrencyCode()))
            throw new IllegalArgumentException("Payment currency must match the reservation currency");
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0)
            throw new IllegalArgumentException("Payment amount must be positive");
        if (payment.getInvoice() != null) {
            if (!"ISSUED".equalsIgnoreCase(payment.getInvoice().getStatus()))
                throw new IllegalStateException("Payments can be posted only to issued invoices");
            if (!payment.getInvoice().getReservation().getId().equals(payment.getReservation().getId()))
                throw new IllegalArgumentException("Invoice belongs to another reservation");
            if (!payment.getInvoice().getHotel().getId().equals(payment.getHotel().getId()))
                throw new IllegalArgumentException("Invoice belongs to another hotel");
        }
    }

    @Override
    @Transactional
    public PaymentDTO allocatePayment(Long paymentId, Long invoiceId) {
        Payment payment = getEntity(paymentId);
        if (isPosted(payment.getStatus()))
            throw new IllegalStateException("Cannot reallocate a posted payment");
        Invoice invoice = invoiceDao.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        if (!"ISSUED".equalsIgnoreCase(invoice.getStatus()))
            throw new IllegalStateException("Payments may be allocated only to issued invoices");
        if (!invoice.getReservation().getId().equals(payment.getReservation().getId()))
            throw new IllegalArgumentException("Payment and invoice belong to different reservations");
        payment.setInvoice(invoice);
        validatePayment(payment);
        return toDto(paymentDao.saveAndFlush(payment));
    }

}
