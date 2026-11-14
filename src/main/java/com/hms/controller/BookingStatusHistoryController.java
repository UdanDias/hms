package com.hms.controller;

import com.hms.dto.BookingStatusHistoryDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.BookingStatusHistoryService;
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
@RequestMapping("/api/v1/bookingstatushistory")
@RequiredArgsConstructor
@Slf4j
public class BookingStatusHistoryController {

    private final BookingStatusHistoryService bookingStatusHistoryService;

    @GetMapping
    public String healthCheck() {
        return "BookingStatusHistory controller is running";
    }

    @GetMapping("/getselectedbookingstatushistory")
    public ResponseEntity<BookingStatusHistoryDTO> getSelectedBookingStatusHistory(
            @RequestParam(name = "bookingStatusHistoryId") Long bookingStatusHistoryId) {
        if (bookingStatusHistoryId == null || bookingStatusHistoryId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            BookingStatusHistoryDTO result = bookingStatusHistoryService.getSelectedBookingStatusHistory(bookingStatusHistoryId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve BookingStatusHistory with ID {}", bookingStatusHistoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallbookingstatushistories")
    public ResponseEntity<List<BookingStatusHistoryDTO>> getAllBookingStatusHistories() {
        try {
            List<BookingStatusHistoryDTO> results = bookingStatusHistoryService.getAllBookingStatusHistories();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve BookingStatusHistories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
