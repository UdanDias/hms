package com.hms.controller;

import com.hms.dto.RoomDTO;
import com.hms.dto.request.*;
import com.hms.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService service;

    @GetMapping
    public String healthCheck() {
        return "Room controller is running";
    }

    @GetMapping("/getselectedroom")
    public ResponseEntity<RoomDTO> getSelectedRoom(@RequestParam("roomId") Long roomId) {
        return ResponseEntity.ok(service.getSelectedRoom(roomId));
    }

    @GetMapping("/getallrooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(service.getAllRooms());
    }

    @PostMapping("/addroom")
    public ResponseEntity<RoomDTO> addRoom(@Valid @RequestBody RoomDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRoom(dto));
    }

    @PutMapping("/updateroom")
    public ResponseEntity<RoomDTO> updateRoom(@RequestParam("roomId") Long roomId, @Valid @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(service.updateRoom(roomId, dto));
    }

    @PatchMapping("/updatehousekeepingstatus")
    public ResponseEntity<RoomDTO> updateHousekeepingStatus(@RequestParam("roomId") Long roomId, @Valid @RequestBody HousekeepingStatusRequestDTO request) {
        return ResponseEntity.ok(service.updateHousekeepingStatus(roomId, request.getStatus()));
    }
}
