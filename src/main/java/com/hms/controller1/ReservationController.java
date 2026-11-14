package com.hms.controller;

import com.hms.dto.ReservationDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public String healthCheck() {
        return reservationService.healthCheck();
    }

    @GetMapping("/getselectedreservation")
    public ResponseEntity<ReservationDTO> getSelectedReservation(@RequestParam(name = "reservationId") Long reservationId) {
        if (reservationId == null || reservationId <= 0) return ResponseEntity.badRequest().build();
        try {
            ReservationDTO result = reservationService.getSelectedReservation(reservationId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Reservation with ID {}", reservationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreservations")
    public ResponseEntity<Page<ReservationDTO>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(reservationService.getAllReservations(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve Reservation records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addreservation")
    public ResponseEntity<ReservationDTO> addReservation(@RequestBody ReservationDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.addReservation(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Reservation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updatereservation")
    public ResponseEntity<ReservationDTO> updateReservation(
            @RequestParam(name = "reservationId") Long reservationId,
            @RequestBody ReservationDTO dto) {
        if (reservationId == null || reservationId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(reservationService.updateReservation(reservationId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Reservation with ID {}", reservationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletereservation")
    public ResponseEntity<Void> deleteReservation(@RequestParam(name = "reservationId") Long reservationId) {
        if (reservationId == null || reservationId <= 0) return ResponseEntity.badRequest().build();
        try {
            reservationService.deleteReservation(reservationId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Reservation with ID {}", reservationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
