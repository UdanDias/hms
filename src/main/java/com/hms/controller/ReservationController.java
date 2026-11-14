package com.hms.controller;

import com.hms.dto.ReservationDTO;
import com.hms.dto.request.*;
import com.hms.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping
    public String healthCheck() {
        return "Reservation controller is running";
    }

    @GetMapping("/getselectedreservation")
    public ResponseEntity<ReservationDTO> getSelectedReservation(@RequestParam("reservationId") Long reservationId) {
        return ResponseEntity.ok(service.getSelectedReservation(reservationId));
    }

    @GetMapping("/getallreservations")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(service.getAllReservations());
    }

    @PostMapping("/addreservation")
    public ResponseEntity<ReservationDTO> addReservation(@Valid @RequestBody ReservationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addReservation(dto));
    }

    @PutMapping("/updatereservation")
    public ResponseEntity<ReservationDTO> updateReservation(@RequestParam("reservationId") Long reservationId, @Valid @RequestBody ReservationDTO dto) {
        return ResponseEntity.ok(service.updateReservation(reservationId, dto));
    }

    @PatchMapping("/confirmreservation")
    public ResponseEntity<ReservationDTO> confirmReservation(@RequestParam("reservationId") Long reservationId) {
        return ResponseEntity.ok(service.confirmReservation(reservationId));
    }

    @PatchMapping("/cancelreservation")
    public ResponseEntity<ReservationDTO> cancelReservation(@RequestParam("reservationId") Long reservationId, @Valid @RequestBody ReasonRequestDTO request) {
        return ResponseEntity.ok(service.cancelReservation(reservationId, request.getReason()));
    }

    @PatchMapping("/checkinreservation")
    public ResponseEntity<ReservationDTO> checkInReservation(@RequestParam("reservationId") Long reservationId) {
        return ResponseEntity.ok(service.checkInReservation(reservationId));
    }

    @PatchMapping("/checkoutreservation")
    public ResponseEntity<ReservationDTO> checkOutReservation(@RequestParam("reservationId") Long reservationId) {
        return ResponseEntity.ok(service.checkOutReservation(reservationId));
    }
}
