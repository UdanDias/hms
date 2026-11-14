package com.hms.controller;

import com.hms.dto.ReservationRoomDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationRoomService;
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
@RequestMapping("/api/v1/reservationroom")
@RequiredArgsConstructor
@Slf4j
public class ReservationRoomController {

    private final ReservationRoomService reservationRoomService;

    @GetMapping
    public String healthCheck() {
        return "ReservationRoom controller is running";
    }

    @GetMapping("/getselectedreservationroom")
    public ResponseEntity<ReservationRoomDTO> getSelectedReservationRoom(
            @RequestParam(name = "reservationRoomId") Long reservationRoomId) {
        if (reservationRoomId == null || reservationRoomId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ReservationRoomDTO result = reservationRoomService.getSelectedReservationRoom(reservationRoomId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationRoom with ID {}", reservationRoomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreservationrooms")
    public ResponseEntity<List<ReservationRoomDTO>> getAllReservationRooms() {
        try {
            List<ReservationRoomDTO> results = reservationRoomService.getAllReservationRooms();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationRooms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
