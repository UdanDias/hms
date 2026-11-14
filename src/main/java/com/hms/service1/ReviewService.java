package com.hms.service;

import com.hms.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
  ReviewDTO getSelectedReview(Long reviewId);
  Page<ReviewDTO> getAllReviews(Pageable pageable);

  ReviewDTO addReview(ReviewDTO dto);

  ReviewDTO updateReview(Long id, ReviewDTO dto);

  void deleteReview(Long id);

  ReviewDTO moderateReview(Long reviewId, String moderationStatus);

  ReviewDTO replyToReview(Long reviewId, String reply);

  default String healthCheck() { return "Review service is running"; }
}
