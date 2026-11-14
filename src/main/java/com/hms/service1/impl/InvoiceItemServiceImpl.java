package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.InvoiceItemDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.InvoiceItemService;
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
 * HMS business service for InvoiceItem.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceItemServiceImpl implements InvoiceItemService {
    private final InvoiceItemDao invoiceItemDao;
    private final InvoiceDao invoiceDao;
    private final ReservationRoomDao reservationRoomDao;
    private final ServiceChargeDao serviceChargeDao;
    private final AccommodationDao accommodationDao;
    @PersistenceContext
    private EntityManager entityManager;

    private InvoiceItem getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid InvoiceItem ID");
        return invoiceItemDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("InvoiceItem not found: " + id));
    }

    private InvoiceItemDTO toDto(InvoiceItem entity) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setInvoiceId(entity.getInvoice() == null ? null : entity.getInvoice().getId());
        dto.setReservationRoomId(entity.getReservationRoom() == null ? null : entity.getReservationRoom().getId());
        dto.setServiceChargeId(entity.getServiceCharge() == null ? null : entity.getServiceCharge().getId());
        dto.setAccommodationId(entity.getAccommodation() == null ? null : entity.getAccommodation().getId());
        return dto;
    }

    private void apply(InvoiceItemDTO dto, InvoiceItem entity) {
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getQuantity() != null) entity.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) entity.setUnitPrice(dto.getUnitPrice());
        if (dto.getDiscountAmount() != null) entity.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getTaxRate() != null) entity.setTaxRate(dto.getTaxRate());
        if (dto.getTaxAmount() != null) entity.setTaxAmount(dto.getTaxAmount());
        if (dto.getServiceChargeAmount() != null) entity.setServiceChargeAmount(dto.getServiceChargeAmount());
        if (dto.getInvoiceId() != null) entity.setInvoice(
                    invoiceDao.findById(dto.getInvoiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + dto.getInvoiceId())));
        if (dto.getReservationRoomId() != null) entity.setReservationRoom(
                    reservationRoomDao.findById(dto.getReservationRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("ReservationRoom not found: " + dto.getReservationRoomId())));
        if (dto.getServiceChargeId() != null) entity.setServiceCharge(
                    serviceChargeDao.findById(dto.getServiceChargeId())
                        .orElseThrow(() -> new ResourceNotFoundException("ServiceCharge not found: " + dto.getServiceChargeId())));
        if (dto.getAccommodationId() != null) entity.setAccommodation(
                    accommodationDao.findById(dto.getAccommodationId())
                        .orElseThrow(() -> new ResourceNotFoundException("AccommodationEntity not found: " + dto.getAccommodationId())));
    }

    @Override
    public InvoiceItemDTO getSelectedInvoiceItem(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<InvoiceItemDTO> getAllInvoiceItems(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return invoiceItemDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public InvoiceItemDTO addInvoiceItem(InvoiceItemDTO dto) {
        if (dto == null) throw new IllegalArgumentException("InvoiceItem DTO is required");
        if (dto.getInvoiceId() == null) throw new IllegalArgumentException("Invoice ID is required");
        InvoiceItem entity = new InvoiceItem();
        apply(dto, entity);
        validateInvoiceItemRelations(entity);
        calculateLineTotal(entity);
        entity = invoiceItemDao.saveAndFlush(entity);
        refreshParentInvoice(entity.getInvoice());
        return toDto(entity);
    }

    @Override
    @Transactional
    public InvoiceItemDTO updateInvoiceItem(Long id, InvoiceItemDTO dto) {
        if (dto == null) throw new IllegalArgumentException("InvoiceItem DTO is required");
        InvoiceItem entity = getEntity(id);
        requireDraft(entity.getInvoice());
        if (dto.getInvoiceId() != null && !dto.getInvoiceId().equals(entity.getInvoice().getId()))
            throw new IllegalArgumentException("Moving an invoice item to another invoice is not supported");
        apply(dto, entity);
        validateInvoiceItemRelations(entity);
        calculateLineTotal(entity);
        entity = invoiceItemDao.saveAndFlush(entity);
        refreshParentInvoice(entity.getInvoice());
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteInvoiceItem(Long id) {
        InvoiceItem entity = getEntity(id);
        requireDraft(entity.getInvoice());
        invoiceItemDao.delete(entity);
        invoiceItemDao.flush();
        refreshParentInvoice(entity.getInvoice());
    }


    private static BigDecimal zero(BigDecimal n) { return n == null ? BigDecimal.ZERO : n; }

    private void requireDraft(Invoice inv) {
        if (inv == null || !"DRAFT".equalsIgnoreCase(inv.getStatus()))
            throw new IllegalStateException("Invoice items can be changed only while invoice is DRAFT");
    }

    private void calculateLineTotal(InvoiceItem item) {
        requireDraft(item.getInvoice());
        BigDecimal qty = zero(item.getQuantity());
        BigDecimal unitPrice = zero(item.getUnitPrice());
        BigDecimal discount = zero(item.getDiscountAmount());
        BigDecimal tax = zero(item.getTaxAmount());
        BigDecimal charge = zero(item.getServiceChargeAmount());
        for (BigDecimal value : new BigDecimal[]{qty,unitPrice,discount,tax,charge})
            if (value.signum() < 0) throw new IllegalArgumentException("Item amounts must not be negative");
        BigDecimal base = qty.multiply(unitPrice);
        if (discount.compareTo(base) > 0) throw new IllegalArgumentException("Discount exceeds line value");
        item.setLineTotal(base.subtract(discount).add(tax).add(charge));
    }

    private void refreshParentInvoice(Invoice inv) {
        if (inv == null || !"DRAFT".equalsIgnoreCase(inv.getStatus())) return;
        List<InvoiceItem> rows = entityManager.createQuery(
            "select i from InvoiceItem i where i.invoice.id = :id", InvoiceItem.class)
            .setParameter("id", inv.getId()).getResultList();
        BigDecimal subtotal = BigDecimal.ZERO, discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO, service = BigDecimal.ZERO;
        for (InvoiceItem row : rows) {
            subtotal = subtotal.add(zero(row.getQuantity()).multiply(zero(row.getUnitPrice())));
            discount = discount.add(zero(row.getDiscountAmount()));
            tax = tax.add(zero(row.getTaxAmount()));
            service = service.add(zero(row.getServiceChargeAmount()));
        }
        inv.setSubtotal(subtotal);
        inv.setDiscountTotal(discount);
        inv.setTaxTotal(tax);
        inv.setServiceChargeTotal(service);
        inv.setGrandTotal(subtotal.subtract(discount).add(tax).add(service));
        invoiceDao.save(inv);
    }

    private void validateInvoiceItemRelations(InvoiceItem item) {
        if (item.getInvoice() == null) throw new IllegalArgumentException("Invoice required");
        Long reservationId = item.getInvoice().getReservation().getId();
        if (item.getReservationRoom() != null &&
            !reservationId.equals(item.getReservationRoom().getReservation().getId()))
            throw new IllegalArgumentException("Invoice item room belongs to another reservation");
        if (item.getServiceCharge() != null &&
            !reservationId.equals(item.getServiceCharge().getReservation().getId()))
            throw new IllegalArgumentException("Invoice item service charge belongs to another reservation");
        if (item.getAccommodation() != null &&
            !item.getAccommodation().getHotel().getId().equals(item.getInvoice().getHotel().getId()))
            throw new IllegalArgumentException("Invoice item accommodation belongs to another hotel");
    }


}
