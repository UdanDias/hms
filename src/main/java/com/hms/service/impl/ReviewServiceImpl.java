package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public ReviewDTO getSelectedReview(Long reviewId) {
    s.actor();
    var e = dao.require(ReviewEntity.class, reviewId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<ReviewDTO> getAllReviews() {
    s.actor();
    return dao.all(ReviewEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReviewDTO addReview(ReviewDTO dto) {
    s.allow("ADMIN", "MANAGER", "RECEPTIONIST", "GUEST");
    dao.lockHotels();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReviewDTO updateReview(Long id, ReviewDTO dto) {
    s.allow("ADMIN", "MANAGER", "RECEPTIONIST", "GUEST");
    dao.lockHotels();
    var old = dao.require(ReviewEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "id", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteReview(Long id) {
    s.allow("ADMIN", "MANAGER", "RECEPTIONIST", "GUEST");
    dao.lockHotels();
    var e = dao.require(ReviewEntity.class, id);
    s.reviewOwner(e.getCustomer());
    s.audit("DELETE", "Review", id);
    dao.remove(e);
    dao.flush();
  }

  private void prepare(ReviewEntity e, ReviewEntity old) {
    if (e.getComment() == null) e.setComment("");
    s.require(
        e.getReservation() != null && e.getCustomer() != null,
        "Reservation and customer are required");
    s.reviewOwner(e.getCustomer());
    s.require(
        "checked_out".equals(e.getReservation().getStatus()), "Reviews require a completed stay");
    s.same(
        e.getReservation().getBookerCustomer().getId(), e.getCustomer().getId(), "Review customer");
    s.require(
        e.getRating() != null && e.getRating() >= 1 && e.getRating() <= 5,
        "Rating must be between 1 and 5");
    if (old != null) {
      s.same(old.getReservation().getId(), e.getReservation().getId(), "Reservation");
      s.same(old.getCustomer().getId(), e.getCustomer().getId(), "Customer");
    }
    e.setModerationStatus("pending");
    e.setModeratedBy(null);
    e.setModeratedAt(null);
    e.setManagementReply(old == null ? null : old.getManagementReply());
    e.setRepliedBy(old == null ? null : old.getRepliedBy());
    e.setRepliedAt(old == null ? null : old.getRepliedAt());
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReviewDTO moderateReview(Long reviewId, String moderationStatus) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    s.require(
        Set.of("approved", "rejected").contains(moderationStatus), "Use approved or rejected");
    var e = dao.require(ReviewEntity.class, reviewId);
    e.setModerationStatus(moderationStatus);
    e.setModeratedBy(s.actor());
    e.setModeratedAt(s.now());
    return finish(e, "MODERATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReviewDTO replyToReview(Long reviewId, String reply) {
    s.allow("ADMIN", "MANAGER");
    dao.lockHotels();
    s.text(reply, "Management reply");
    var e = dao.require(ReviewEntity.class, reviewId);
    e.setManagementReply(reply);
    e.setRepliedBy(s.actor());
    e.setRepliedAt(s.now());
    return finish(e, "REPLY");
  }

  private ReviewDTO finish(ReviewEntity e, String action) {
    s.validate(e);
    s.audit(action, "Review", e.getId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
