package com.hms.controller;

import com.hms.dto.ReviewDTO;
import com.hms.dto.request.*;
import com.hms.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @GetMapping
    public String healthCheck() {
        return "Review controller is running";
    }

    @GetMapping("/getselectedreview")
    public ResponseEntity<ReviewDTO> getSelectedReview(@RequestParam("reviewId") Long reviewId) {
        return ResponseEntity.ok(service.getSelectedReview(reviewId));
    }

    @GetMapping("/getallreviews")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(service.getAllReviews());
    }

    @PostMapping("/addreview")
    public ResponseEntity<ReviewDTO> addReview(@Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addReview(dto));
    }

    @PutMapping("/updatereview")
    public ResponseEntity<ReviewDTO> updateReview(@RequestParam("reviewId") Long reviewId, @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(service.updateReview(reviewId, dto));
    }

    @DeleteMapping("/deletereview")
    public ResponseEntity<Void> deleteReview(@RequestParam("reviewId") Long reviewId) {
        service.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/moderatereview")
    public ResponseEntity<ReviewDTO> moderateReview(@RequestParam("reviewId") Long reviewId, @Valid @RequestBody ReviewModerationRequestDTO request) {
        return ResponseEntity.ok(service.moderateReview(reviewId, request.getModerationStatus()));
    }

    @PatchMapping("/replytoreview")
    public ResponseEntity<ReviewDTO> replyToReview(@RequestParam("reviewId") Long reviewId, @Valid @RequestBody ReviewReplyRequestDTO request) {
        return ResponseEntity.ok(service.replyToReview(reviewId, request.getReply()));
    }
}
