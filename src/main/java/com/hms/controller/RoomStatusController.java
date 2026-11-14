package com.hms.controller;

import com.hms.dto.RoomStatusDTO;
import com.hms.service.RoomStatusService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roomstatus")
@RequiredArgsConstructor
public class RoomStatusController {

    private final RoomStatusService service;

    @GetMapping
    public String healthCheck() {
        return "RoomStatus controller is running";
    }

    @GetMapping("/getselectedroomstatus")
    public ResponseEntity<RoomStatusDTO> getSelectedRoomStatus(@RequestParam("roomStatusId") Long roomStatusId) {
        return ResponseEntity.ok(service.getSelectedRoomStatus(roomStatusId));
    }

    @GetMapping("/getallroomstatuses")
    public ResponseEntity<List<RoomStatusDTO>> getAllRoomStatuses() {
        return ResponseEntity.ok(service.getAllRoomStatuses());
    }

    @PostMapping("/addroomstatus")
    public ResponseEntity<RoomStatusDTO> addRoomStatus(@Valid @RequestBody RoomStatusDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRoomStatus(dto));
    }
}
