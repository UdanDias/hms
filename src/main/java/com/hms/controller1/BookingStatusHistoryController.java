package com.hms.controller;

import com.hms.dto.BookingStatusHistoryDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.BookingStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/bookingstatushistory")
@RequiredArgsConstructor
@Slf4j
public class BookingStatusHistoryController {

    private final BookingStatusHistoryService bookingStatusHistoryService;

    @GetMapping
    public String healthCheck() {
        return bookingStatusHistoryService.healthCheck();
    }

    @GetMapping("/getselectedbookingstatushistory")
    public ResponseEntity<BookingStatusHistoryDTO> getSelectedBookingStatusHistory(@RequestParam(name = "bookingStatusHistoryId") Long bookingStatusHistoryId) {
        if (bookingStatusHistoryId == null || bookingStatusHistoryId <= 0) return ResponseEntity.badRequest().build();
        try {
            BookingStatusHistoryDTO result = bookingStatusHistoryService.getSelectedBookingStatusHistory(bookingStatusHistoryId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve BookingStatusHistory with ID {}", bookingStatusHistoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallbookingstatushistories")
    public ResponseEntity<Page<BookingStatusHistoryDTO>> getAllBookingStatusHistories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(bookingStatusHistoryService.getAllBookingStatusHistories(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve BookingStatusHistory records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addbookingstatushistory")
    public ResponseEntity<BookingStatusHistoryDTO> addBookingStatusHistory(@RequestBody BookingStatusHistoryDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(bookingStatusHistoryService.addBookingStatusHistory(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add BookingStatusHistory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updatebookingstatushistory")
    public ResponseEntity<BookingStatusHistoryDTO> updateBookingStatusHistory(
            @RequestParam(name = "bookingStatusHistoryId") Long bookingStatusHistoryId,
            @RequestBody BookingStatusHistoryDTO dto) {
        if (bookingStatusHistoryId == null || bookingStatusHistoryId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(bookingStatusHistoryService.updateBookingStatusHistory(bookingStatusHistoryId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update BookingStatusHistory with ID {}", bookingStatusHistoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletebookingstatushistory")
    public ResponseEntity<Void> deleteBookingStatusHistory(@RequestParam(name = "bookingStatusHistoryId") Long bookingStatusHistoryId) {
        if (bookingStatusHistoryId == null || bookingStatusHistoryId <= 0) return ResponseEntity.badRequest().build();
        try {
            bookingStatusHistoryService.deleteBookingStatusHistory(bookingStatusHistoryId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete BookingStatusHistory with ID {}", bookingStatusHistoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
