package com.hms.controller;

import com.hms.dto.ReservationDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationService;
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
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public String healthCheck() {
        return "Reservation controller is running";
    }

    @GetMapping("/getselectedreservation")
    public ResponseEntity<ReservationDTO> getSelectedReservation(
            @RequestParam(name = "reservationId") Long reservationId) {
        if (reservationId == null || reservationId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ReservationDTO result = reservationService.getSelectedReservation(reservationId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Reservation with ID {}", reservationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreservations")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        try {
            List<ReservationDTO> results = reservationService.getAllReservations();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
