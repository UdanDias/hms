package com.hms.service.impl;

import com.hms.dao.CustomerDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dao.ReviewDAO;
import com.hms.dto.ReviewDTO;
import com.hms.entities.CustomerEntity;
import com.hms.entities.ReservationEntity;
import com.hms.entities.ReviewEntity;
import com.hms.exception.CustomerNotFoundException;
import com.hms.exception.ReservationNotFoundException;
import com.hms.exception.ReviewNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.ReviewService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.UtilData;
import com.hms.util.ValidationUtil;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ReviewServiceImpl implements ReviewService {
  private final CustomerDAO customerDao;
  private final ReviewDAO reviewDao;
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public ReviewDTO getSelectedReview(Long reviewId) {
    serviceSecurity.getCurrentUser();
    ReviewEntity reviewEntity =
        reviewDao
            .findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
    serviceSecurity.checkRead(reviewEntity);
    return entityDTOConvert.convertReviewEntityToReviewDTO(reviewEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewDTO> getAllReviews() {
    serviceSecurity.getCurrentUser();
    List<ReviewEntity> entities =
        reviewDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertReviewEntityListToReviewDTOList(entities);
  }

  @Override
  public ReviewDTO addReview(ReviewDTO reviewDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST", "GUEST");
    hotelDao.lockAllForWrite();
    ReviewEntity reviewEntity = entityDTOConvert.convertReviewDTOToReviewEntity(reviewDTO);
    if (reviewDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(reviewDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      reviewEntity.setReservation(reservation);
    }
    if (reviewDTO.getCustomerId() != null) {
      CustomerEntity customer =
          customerDao
              .findById(reviewDTO.getCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      reviewEntity.setCustomer(customer);
    }

    validateReviewForSave(reviewEntity, null);
    validationUtil.validate(reviewEntity);
    reviewDao.save(reviewEntity);
    auditRecorder.recordAction("CREATE", "Review", reviewEntity.getId());
    reviewDao.flush();
    return entityDTOConvert.convertReviewEntityToReviewDTO(reviewEntity);
  }

  @Override
  public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST", "GUEST");
    hotelDao.lockAllForWrite();
    ReviewEntity reviewEntity =
        reviewDao
            .findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
    ReviewEntity requestedReview = entityDTOConvert.convertReviewDTOToReviewEntity(reviewDTO);
    if (reviewDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(reviewDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      requestedReview.setReservation(reservation);
    }
    if (reviewDTO.getCustomerId() != null) {
      CustomerEntity customer =
          customerDao
              .findById(reviewDTO.getCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      requestedReview.setCustomer(customer);
    }

    validateReviewForSave(requestedReview, reviewEntity);
    validationUtil.validate(requestedReview);
    reviewEntity.setReservation(requestedReview.getReservation());
    reviewEntity.setCustomer(requestedReview.getCustomer());
    reviewEntity.setRating(requestedReview.getRating());
    reviewEntity.setTitle(requestedReview.getTitle());
    reviewEntity.setComment(requestedReview.getComment());
    reviewEntity.setModerationStatus(requestedReview.getModerationStatus());
    reviewEntity.setModeratedBy(requestedReview.getModeratedBy());
    reviewEntity.setModeratedAt(requestedReview.getModeratedAt());
    reviewEntity.setManagementReply(requestedReview.getManagementReply());
    reviewEntity.setRepliedBy(requestedReview.getRepliedBy());
    reviewEntity.setRepliedAt(requestedReview.getRepliedAt());
    validationUtil.validate(reviewEntity);
    reviewDao.save(reviewEntity);
    auditRecorder.recordAction("UPDATE", "Review", reviewEntity.getId());
    reviewDao.flush();
    return entityDTOConvert.convertReviewEntityToReviewDTO(reviewEntity);
  }

  @Override
  public void deleteReview(Long reviewId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST", "GUEST");
    hotelDao.lockAllForWrite();
    ReviewEntity reviewEntity =
        reviewDao
            .findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
    serviceSecurity.checkReviewOwner(reviewEntity.getCustomer());
    auditRecorder.recordAction("DELETE", "Review", reviewId);
    reviewDao.delete(reviewEntity);
    reviewDao.flush();
  }

  private void validateReviewForSave(ReviewEntity reviewEntity, ReviewEntity existingReview) {
    if (reviewEntity.getComment() == null) reviewEntity.setComment("");
    validationUtil.require(
        reviewEntity.getReservation() != null && reviewEntity.getCustomer() != null,
        "Reservation and customer are required");
    serviceSecurity.checkReviewOwner(reviewEntity.getCustomer());
    validationUtil.require(
        "checked_out".equals(reviewEntity.getReservation().getStatus()),
        "Reviews require a completed stay");
    validationUtil.same(
        reviewEntity.getReservation().getBookerCustomer().getId(),
        reviewEntity.getCustomer().getId(),
        "Review customer");
    validationUtil.require(
        reviewEntity.getRating() != null
            && reviewEntity.getRating() >= 1
            && reviewEntity.getRating() <= 5,
        "Rating must be between 1 and 5");
    if (existingReview != null) {
      validationUtil.same(
          existingReview.getReservation().getId(),
          reviewEntity.getReservation().getId(),
          "Reservation");
      validationUtil.same(
          existingReview.getCustomer().getId(), reviewEntity.getCustomer().getId(), "Customer");
    }
    reviewEntity.setModerationStatus("pending");
    reviewEntity.setModeratedBy(null);
    reviewEntity.setModeratedAt(null);
    reviewEntity.setManagementReply(
        existingReview == null ? null : existingReview.getManagementReply());
    reviewEntity.setRepliedBy(existingReview == null ? null : existingReview.getRepliedBy());
    reviewEntity.setRepliedAt(existingReview == null ? null : existingReview.getRepliedAt());
  }

  @Override
  public ReviewDTO moderateReview(Long reviewId, String moderationStatus) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    validationUtil.require(
        Set.of("approved", "rejected").contains(moderationStatus), "Use approved or rejected");
    ReviewEntity reviewEntity =
        reviewDao
            .findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
    reviewEntity.setModerationStatus(moderationStatus);
    reviewEntity.setModeratedBy(serviceSecurity.getCurrentUser());
    reviewEntity.setModeratedAt(UtilData.generateNow());
    validationUtil.validate(reviewEntity);
    auditRecorder.recordAction("MODERATE", "Review", reviewEntity.getId());
    reviewDao.flush();
    return entityDTOConvert.convertReviewEntityToReviewDTO(reviewEntity);
  }

  @Override
  public ReviewDTO replyToReview(Long reviewId, String reply) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER");
    hotelDao.lockAllForWrite();
    validationUtil.text(reply, "Management reply");
    ReviewEntity reviewEntity =
        reviewDao
            .findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
    reviewEntity.setManagementReply(reply);
    reviewEntity.setRepliedBy(serviceSecurity.getCurrentUser());
    reviewEntity.setRepliedAt(UtilData.generateNow());
    validationUtil.validate(reviewEntity);
    auditRecorder.recordAction("REPLY", "Review", reviewEntity.getId());
    reviewDao.flush();
    return entityDTOConvert.convertReviewEntityToReviewDTO(reviewEntity);
  }
}
