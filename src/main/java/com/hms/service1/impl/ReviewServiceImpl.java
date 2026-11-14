package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.ReviewDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReviewService;
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
 * HMS business service for Review.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    private final ReviewDao reviewDao;
    private final ReservationDao reservationDao;
    private final CustomerDao customerDao;
    private final UserDao userDao;

    private Review getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Review ID");
        return reviewDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + id));
    }

    private ReviewDTO toDto(Review entity) {
        ReviewDTO dto = new ReviewDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setReservationId(entity.getReservation() == null ? null : entity.getReservation().getId());
        dto.setCustomerId(entity.getCustomer() == null ? null : entity.getCustomer().getId());
        dto.setModeratedById(entity.getModeratedBy() == null ? null : entity.getModeratedBy().getUserId());
        dto.setRepliedById(entity.getRepliedBy() == null ? null : entity.getRepliedBy().getUserId());
        return dto;
    }

    private void apply(ReviewDTO dto, Review entity) {
        if (dto.getRating() != null) entity.setRating(dto.getRating());
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getComment() != null) entity.setComment(dto.getComment());
        if (dto.getReservationId() != null) entity.setReservation(
                    reservationDao.findById(dto.getReservationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId())));
        if (dto.getCustomerId() != null) entity.setCustomer(
                    customerDao.findById(dto.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("CustomerEntity not found: " + dto.getCustomerId())));
    }

    @Override
    public ReviewDTO getSelectedReview(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<ReviewDTO> getAllReviews(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return reviewDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public ReviewDTO addReview(ReviewDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Review DTO is required");
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) throw new IllegalArgumentException("Rating must be 1 to 5");
        Review entity = new Review();
        apply(dto, entity);
        verifyReviewEligibility(entity);
        entity.setModerationStatus("PENDING");
        entity.setModeratedAt(null);
        entity.setRepliedAt(null);
        entity = reviewDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Review DTO is required");
        Review entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        verifyReviewEligibility(entity);
        if (entity.getRating() == null || entity.getRating() < 1 || entity.getRating() > 5) throw new IllegalArgumentException("Rating must be 1 to 5");
        entity = reviewDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review entity = getEntity(id);
        // Nothing extra.
        reviewDao.delete(entity);
        reviewDao.flush();
        // Nothing extra.
    }


    @Override @Transactional
    public ReviewDTO moderateReview(Long reviewId, String moderationStatus) {
        if (moderationStatus == null || !Set.of("APPROVED", "REJECTED").contains(moderationStatus.toUpperCase(Locale.ROOT)))
            throw new IllegalArgumentException("Moderation status must be APPROVED or REJECTED");
        Review review = getEntity(reviewId);
        review.setModerationStatus(moderationStatus.toUpperCase(Locale.ROOT));
        review.setModeratedAt(LocalDateTime.now(ZoneOffset.UTC));
        // TODO: Derive moderatedBy from the authenticated user, not from client input.
        return toDto(reviewDao.saveAndFlush(review));
    }

    @Override @Transactional
    public ReviewDTO replyToReview(Long reviewId, String reply) {
        if (reply == null || reply.isBlank()) throw new IllegalArgumentException("Reply is required");
        Review review = getEntity(reviewId);
        if (!"APPROVED".equalsIgnoreCase(review.getModerationStatus()))
            throw new IllegalStateException("Review must be approved before replying");
        review.setManagementReply(reply.trim());
        review.setRepliedAt(LocalDateTime.now(ZoneOffset.UTC));
        // TODO: Derive repliedBy from authenticated management account.
        return toDto(reviewDao.saveAndFlush(review));
    }

    private void verifyReviewEligibility(Review review) {
        if (review.getReservation() == null || review.getCustomer() == null)
            throw new IllegalArgumentException("Review reservation and customer are required");
        if (!"CHECKED_OUT".equalsIgnoreCase(review.getReservation().getStatus()))
            throw new IllegalStateException("Reviews are allowed only after checkout");
        if (!review.getCustomer().getId().equals(review.getReservation().getBookerCustomer().getId()))
            throw new IllegalArgumentException("Only the booking customer can post a review");
    }


}
