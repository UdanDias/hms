package com.hms.service;

import com.hms.dto.ReviewDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface ReviewService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    ReviewDTO getSelectedReview(Long reviewId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<ReviewDTO> getAllReviews();
}
