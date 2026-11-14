package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.RefundDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RefundService;
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
 * HMS business service for Refund.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundServiceImpl implements RefundService {
    private final RefundDao refundDao;
    private final PaymentDao paymentDao;
    private final UserDao userDao;
    @PersistenceContext
    private EntityManager entityManager;

    private Refund getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Refund ID");
        return refundDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + id));
    }

    private RefundDTO toDto(Refund entity) {
        RefundDTO dto = new RefundDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setPaymentId(entity.getPayment() == null ? null : entity.getPayment().getId());
        dto.setProcessedById(entity.getProcessedBy() == null ? null : entity.getProcessedBy().getUserId());
        return dto;
    }

    private void apply(RefundDTO dto, Refund entity) {
        if (dto.getRefundNumber() != null) entity.setRefundNumber(dto.getRefundNumber());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
        if (dto.getRefundMethod() != null) entity.setRefundMethod(dto.getRefundMethod());
        if (dto.getReferenceNumber() != null) entity.setReferenceNumber(dto.getReferenceNumber());
        if (dto.getReason() != null) entity.setReason(dto.getReason());
        // Refund settlement state is controlled by a trusted payment processor.
        if (dto.getPaymentId() != null) entity.setPayment(
                    paymentDao.findById(dto.getPaymentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + dto.getPaymentId())));
        if (dto.getProcessedById() != null) entity.setProcessedBy(
                    userDao.findById(dto.getProcessedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getProcessedById())));
    }

    @Override
    public RefundDTO getSelectedRefund(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<RefundDTO> getAllRefunds(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return refundDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public RefundDTO addRefund(RefundDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Refund DTO is required");
        if (dto.getPaymentId() == null || dto.getRefundNumber() == null || dto.getRefundNumber().isBlank()) throw new IllegalArgumentException("Payment ID and refund number are required");
        Refund entity = new Refund();
        apply(dto, entity);
        entity.setStatus("PENDING");
        entity.setRefundedAt(null);
        validateRefund(entity);
        entity = refundDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public RefundDTO updateRefund(Long id, RefundDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Refund DTO is required");
        Refund entity = getEntity(id);
        if (isPosted(entity.getStatus())) throw new IllegalStateException("Posted refunds are immutable");
        apply(dto, entity);
        validateRefund(entity);
        entity = refundDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteRefund(Long id) {
        Refund entity = getEntity(id);
        if (isPosted(entity.getStatus())) throw new IllegalStateException("Posted refunds cannot be deleted");
        refundDao.delete(entity);
        refundDao.flush();
        // Nothing extra.
    }


    private static final Set<String> SETTLED = Set.of("COMPLETED", "PAID", "SUCCESS", "SETTLED");
    private static String upper(String s) {return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);}
    private static boolean isPosted(String status) {return SETTLED.contains(upper(status));}

    private void validateRefund(Refund refund) {
        if (refund.getAmount() == null || refund.getAmount().signum() <= 0)
            throw new IllegalArgumentException("Refund amount must be positive");
        Payment payment = refund.getPayment();
        if (payment != null) {
            payment = entityManager.find(Payment.class, payment.getId(),
                jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        }
        if (payment == null || !isPosted(payment.getStatus()))
            throw new IllegalStateException("Only a settled payment can be refunded");
        BigDecimal allocated = entityManager.createQuery(
            "select sum(r.amount) from Refund r where r.payment.id = :paymentId " +
            "and r.id <> :refundId and upper(r.status) <> 'REJECTED' " +
            "and upper(r.status) <> 'CANCELLED'", BigDecimal.class)
            .setParameter("paymentId", payment.getId())
            .setParameter("refundId",refund.getId() == null ? -1L : refund.getId())
            .getSingleResult();
        if (refund.getAmount().add(allocated == null ? BigDecimal.ZERO : allocated).compareTo(payment.getAmount()) > 0)
            throw new IllegalStateException("Refunds exceed the original payment");
    }

}
