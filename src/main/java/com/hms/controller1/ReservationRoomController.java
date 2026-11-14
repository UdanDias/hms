package com.hms.controller;

import com.hms.dto.ReservationRoomDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/reservationroom")
@RequiredArgsConstructor
@Slf4j
public class ReservationRoomController {

    private final ReservationRoomService reservationRoomService;

    @GetMapping
    public String healthCheck() {
        return reservationRoomService.healthCheck();
    }

    @GetMapping("/getselectedreservationroom")
    public ResponseEntity<ReservationRoomDTO> getSelectedReservationRoom(@RequestParam(name = "reservationRoomId") Long reservationRoomId) {
        if (reservationRoomId == null || reservationRoomId <= 0) return ResponseEntity.badRequest().build();
        try {
            ReservationRoomDTO result = reservationRoomService.getSelectedReservationRoom(reservationRoomId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationRoom with ID {}", reservationRoomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreservationrooms")
    public ResponseEntity<Page<ReservationRoomDTO>> getAllReservationRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(reservationRoomService.getAllReservationRooms(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationRoom records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addreservationroom")
    public ResponseEntity<ReservationRoomDTO> addReservationRoom(@RequestBody ReservationRoomDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationRoomService.addReservationRoom(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add ReservationRoom", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updatereservationroom")
    public ResponseEntity<ReservationRoomDTO> updateReservationRoom(
            @RequestParam(name = "reservationRoomId") Long reservationRoomId,
            @RequestBody ReservationRoomDTO dto) {
        if (reservationRoomId == null || reservationRoomId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(reservationRoomService.updateReservationRoom(reservationRoomId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update ReservationRoom with ID {}", reservationRoomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletereservationroom")
    public ResponseEntity<Void> deleteReservationRoom(@RequestParam(name = "reservationRoomId") Long reservationRoomId) {
        if (reservationRoomId == null || reservationRoomId <= 0) return ResponseEntity.badRequest().build();
        try {
            reservationRoomService.deleteReservationRoom(reservationRoomId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete ReservationRoom with ID {}", reservationRoomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
