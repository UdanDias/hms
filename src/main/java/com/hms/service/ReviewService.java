package com.hms.service;

import com.hms.dto.ReviewDTO;
import java.util.List;

public interface ReviewService {
  ReviewDTO getSelectedReview(Long reviewId);

  List<ReviewDTO> getAllReviews();

  ReviewDTO addReview(ReviewDTO dto);

  ReviewDTO updateReview(Long reviewId, ReviewDTO dto);

  void deleteReview(Long reviewId);

  ReviewDTO moderateReview(Long reviewId, String moderationStatus);

  ReviewDTO replyToReview(Long reviewId, String reply);
}
