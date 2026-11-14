package com.hms.controller;

import com.hms.dto.ReviewDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String healthCheck() {
        return "Review controller is running";
    }

    @GetMapping("/getselectedreview")
    public ResponseEntity<ReviewDTO> getSelectedReview(
            @RequestParam(name = "reviewId") Long reviewId) {
        if (reviewId == null || reviewId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ReviewDTO result = reviewService.getSelectedReview(reviewId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Review with ID {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreviews")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        try {
            List<ReviewDTO> results = reviewService.getAllReviews();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
