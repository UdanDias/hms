package com.hms.controller;

import com.hms.dto.ReservationRoomDTO;
import com.hms.dto.request.*;
import com.hms.service.ReservationRoomService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservationroom")
@RequiredArgsConstructor
public class ReservationRoomController {

    private final ReservationRoomService service;

    @GetMapping
    public String healthCheck() {
        return "ReservationRoom controller is running";
    }

    @GetMapping("/getselectedreservationroom")
    public ResponseEntity<ReservationRoomDTO> getSelectedReservationRoom(@RequestParam("reservationRoomId") Long reservationRoomId) {
        return ResponseEntity.ok(service.getSelectedReservationRoom(reservationRoomId));
    }

    @GetMapping("/getallreservationrooms")
    public ResponseEntity<List<ReservationRoomDTO>> getAllReservationRooms() {
        return ResponseEntity.ok(service.getAllReservationRooms());
    }

    @PostMapping("/addreservationroom")
    public ResponseEntity<ReservationRoomDTO> addReservationRoom(@Valid @RequestBody ReservationRoomDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addReservationRoom(dto));
    }

    @PutMapping("/updatereservationroom")
    public ResponseEntity<ReservationRoomDTO> updateReservationRoom(@RequestParam("reservationRoomId") Long reservationRoomId, @Valid @RequestBody ReservationRoomDTO dto) {
        return ResponseEntity.ok(service.updateReservationRoom(reservationRoomId, dto));
    }

    @PatchMapping("/cancelreservationroom")
    public ResponseEntity<ReservationRoomDTO> cancelReservationRoom(@RequestParam("reservationRoomId") Long reservationRoomId, @Valid @RequestBody ReasonRequestDTO request) {
        return ResponseEntity.ok(service.cancelReservationRoom(reservationRoomId, request.getReason()));
    }

    @PatchMapping("/assignroom")
    public ResponseEntity<ReservationRoomDTO> assignRoom(@RequestParam("reservationRoomId") Long reservationRoomId, @Valid @RequestBody RoomAssignmentRequestDTO request) {
        return ResponseEntity.ok(service.assignRoom(reservationRoomId, request.getRoomId()));
    }
}
