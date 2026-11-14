package com.hms.controller;

import com.hms.dto.BookingStatusHistoryDTO;
import com.hms.service.BookingStatusHistoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookingstatushistory")
@RequiredArgsConstructor
public class BookingStatusHistoryController {

    private final BookingStatusHistoryService service;

    @GetMapping
    public String healthCheck() {
        return "BookingStatusHistory controller is running";
    }

    @GetMapping("/getselectedbookingstatushistory")
    public ResponseEntity<BookingStatusHistoryDTO> getSelectedBookingStatusHistory(@RequestParam("bookingStatusHistoryId") Long bookingStatusHistoryId) {
        return ResponseEntity.ok(service.getSelectedBookingStatusHistory(bookingStatusHistoryId));
    }

    @GetMapping("/getallbookingstatushistories")
    public ResponseEntity<List<BookingStatusHistoryDTO>> getAllBookingStatusHistories() {
        return ResponseEntity.ok(service.getAllBookingStatusHistories());
    }
}
