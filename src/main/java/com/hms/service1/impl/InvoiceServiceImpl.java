package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.InvoiceDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.InvoiceService;
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
 * HMS business service for Invoice.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceDao invoiceDao;
    private final HotelDao hotelDao;
    private final ReservationDao reservationDao;
    private final CustomerDao customerDao;
    private final UserDao userDao;
    @PersistenceContext
    private EntityManager entityManager;

    private Invoice getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Invoice ID");
        return invoiceDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    private InvoiceDTO toDto(Invoice entity) {
        InvoiceDTO dto = new InvoiceDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        dto.setReservationId(entity.getReservation() == null ? null : entity.getReservation().getId());
        dto.setBilledCustomerId(entity.getBilledCustomer() == null ? null : entity.getBilledCustomer().getId());
        dto.setCreatedById(entity.getCreatedBy() == null ? null : entity.getCreatedBy().getUserId());
        return dto;
    }

    private void apply(InvoiceDTO dto, Invoice entity) {
        if (dto.getInvoiceNumber() != null) entity.setInvoiceNumber(dto.getInvoiceNumber());
        if (dto.getBillingNameSnapshot() != null) entity.setBillingNameSnapshot(dto.getBillingNameSnapshot());
        if (dto.getBillingAddressSnapshot() != null) entity.setBillingAddressSnapshot(dto.getBillingAddressSnapshot());
        if (dto.getCurrencyCode() != null) entity.setCurrencyCode(dto.getCurrencyCode());
        if (dto.getDueDate() != null) entity.setDueDate(dto.getDueDate());
        if (dto.getSubtotal() != null) entity.setSubtotal(dto.getSubtotal());
        if (dto.getDiscountTotal() != null) entity.setDiscountTotal(dto.getDiscountTotal());
        if (dto.getTaxTotal() != null) entity.setTaxTotal(dto.getTaxTotal());
        if (dto.getServiceChargeTotal() != null) entity.setServiceChargeTotal(dto.getServiceChargeTotal());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
        if (dto.getReservationId() != null) entity.setReservation(
                    reservationDao.findById(dto.getReservationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId())));
        if (dto.getBilledCustomerId() != null) entity.setBilledCustomer(
                    customerDao.findById(dto.getBilledCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("CustomerEntity not found: " + dto.getBilledCustomerId())));
        if (dto.getCreatedById() != null) entity.setCreatedBy(
                    userDao.findById(dto.getCreatedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getCreatedById())));
    }

    @Override
    public InvoiceDTO getSelectedInvoice(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<InvoiceDTO> getAllInvoices(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return invoiceDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public InvoiceDTO addInvoice(InvoiceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Invoice DTO is required");
        if (dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()) throw new IllegalArgumentException("Invoice number is required");
        Invoice entity = new Invoice();
        apply(dto, entity);
        entity.setStatus("DRAFT");
        entity.setIssuedAt(null);
        validateInvoiceRelations(entity);
        updateTotals(entity);
        entity = invoiceDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Invoice DTO is required");
        Invoice entity = getEntity(id);
        requireDraft(entity);
        apply(dto, entity);
        validateInvoiceRelations(entity);
        updateTotals(entity);
        entity = invoiceDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        Invoice entity = getEntity(id);
        requireDraft(entity);
        invoiceDao.delete(entity);
        invoiceDao.flush();
        // Nothing extra.
    }


    private static final Set<String> SETTLED = Set.of("COMPLETED", "PAID", "SUCCESS", "SETTLED");

    private void requireDraft(Invoice invoice) {
        if (!"DRAFT".equalsIgnoreCase(invoice.getStatus()))
            throw new IllegalStateException("Only draft invoices may be edited");
    }

    private void updateTotals(Invoice invoice) {
        BigDecimal subtotal = zero(invoice.getSubtotal());
        BigDecimal discounts = zero(invoice.getDiscountTotal());
        BigDecimal taxes = zero(invoice.getTaxTotal());
        BigDecimal charges = zero(invoice.getServiceChargeTotal());
        for (BigDecimal val : new BigDecimal[]{subtotal,discounts,taxes,charges})
            if (val.signum() < 0) throw new IllegalArgumentException("Invoice amounts must be nonnegative");
        BigDecimal grand = subtotal.subtract(discounts).add(taxes).add(charges);
        if (grand.signum() < 0) throw new IllegalArgumentException("Invoice discount exceeds total");
        invoice.setSubtotal(subtotal);
        invoice.setDiscountTotal(discounts);
        invoice.setTaxTotal(taxes);
        invoice.setServiceChargeTotal(charges);
        invoice.setGrandTotal(grand);
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    @Override
    @Transactional
    public InvoiceDTO issueInvoice(Long invoiceId) {
        Invoice invoice = getEntity(invoiceId);
        requireDraft(invoice);
        // Calculate invoice from its items when available, rather than trusting client totals.
        List<InvoiceItem> items = entityManager.createQuery(
            "select i from InvoiceItem i where i.invoice.id = :invoiceId", InvoiceItem.class)
            .setParameter("invoiceId", invoiceId).getResultList();
        if (items.isEmpty()) throw new IllegalStateException("Cannot issue an invoice without items");
        BigDecimal subtotal = BigDecimal.ZERO, discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO, charges = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            subtotal = subtotal.add(zero(item.getQuantity()).multiply(zero(item.getUnitPrice())));
            discount = discount.add(zero(item.getDiscountAmount()));
            tax = tax.add(zero(item.getTaxAmount()));
            charges = charges.add(zero(item.getServiceChargeAmount()));
        }
        invoice.setSubtotal(subtotal);
        invoice.setDiscountTotal(discount);
        invoice.setTaxTotal(tax);
        invoice.setServiceChargeTotal(charges);
        updateTotals(invoice);
        invoice.setStatus("ISSUED");
        invoice.setIssuedAt(LocalDateTime.now(ZoneOffset.UTC));
        return toDto(invoiceDao.saveAndFlush(invoice));
    }

    @Override
    @Transactional
    public InvoiceDTO voidInvoice(Long invoiceId) {
        Invoice invoice = getEntity(invoiceId);
        if ("VOID".equalsIgnoreCase(invoice.getStatus())) return toDto(invoice);
        if (getSettledPayments(invoiceId).signum() > 0)
            throw new IllegalStateException("Refund/reverse settled payments before voiding the invoice");
        invoice.setStatus("VOID");
        return toDto(invoiceDao.saveAndFlush(invoice));
    }

    private BigDecimal getSettledPayments(Long invoiceId) {
        BigDecimal paid = entityManager.createQuery(
            "select sum(p.amount) from Payment p where p.invoice.id = :invoiceId " +
            "and upper(p.status) in :statuses", BigDecimal.class)
            .setParameter("invoiceId",invoiceId).setParameter("statuses",SETTLED)
            .getSingleResult();
        BigDecimal refunded = entityManager.createQuery(
            "select sum(r.amount) from Refund r where r.payment.invoice.id = :invoiceId " +
            "and upper(r.status) in :statuses", BigDecimal.class)
            .setParameter("invoiceId",invoiceId).setParameter("statuses",SETTLED)
            .getSingleResult();
        return zero(paid).subtract(zero(refunded)).max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getOutstandingBalance(Long invoiceId) {
        Invoice invoice = getEntity(invoiceId);
        if ("VOID".equalsIgnoreCase(invoice.getStatus())) return BigDecimal.ZERO;
        return zero(invoice.getGrandTotal()).subtract(getSettledPayments(invoiceId)).max(BigDecimal.ZERO);
    }

    private void validateInvoiceRelations(Invoice invoice) {
        if (invoice.getHotel() == null || invoice.getReservation() == null)
            throw new IllegalArgumentException("Invoice hotel and reservation are required");
        if (!invoice.getHotel().getId().equals(invoice.getReservation().getHotel().getId()))
            throw new IllegalArgumentException("Invoice reservation belongs to another hotel");
        if (invoice.getCurrencyCode() == null ||
            !invoice.getCurrencyCode().equalsIgnoreCase(invoice.getReservation().getCurrencyCode()))
            throw new IllegalArgumentException("Invoice and reservation currencies must match");
    }


}
