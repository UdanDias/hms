package com.hms.controller;

import com.hms.dto.RoomRateDTO;
import com.hms.service.RoomRateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roomrate")
@RequiredArgsConstructor
public class RoomRateController {

    private final RoomRateService service;

    @GetMapping
    public String healthCheck() {
        return "RoomRate controller is running";
    }

    @GetMapping("/getselectedroomrate")
    public ResponseEntity<RoomRateDTO> getSelectedRoomRate(@RequestParam("roomRateId") Long roomRateId) {
        return ResponseEntity.ok(service.getSelectedRoomRate(roomRateId));
    }

    @GetMapping("/getallroomrates")
    public ResponseEntity<List<RoomRateDTO>> getAllRoomRates() {
        return ResponseEntity.ok(service.getAllRoomRates());
    }

    @PostMapping("/addroomrate")
    public ResponseEntity<RoomRateDTO> addRoomRate(@Valid @RequestBody RoomRateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRoomRate(dto));
    }

    @PutMapping("/updateroomrate")
    public ResponseEntity<RoomRateDTO> updateRoomRate(@RequestParam("roomRateId") Long roomRateId, @Valid @RequestBody RoomRateDTO dto) {
        return ResponseEntity.ok(service.updateRoomRate(roomRateId, dto));
    }

    @DeleteMapping("/deleteroomrate")
    public ResponseEntity<Void> deleteRoomRate(@RequestParam("roomRateId") Long roomRateId) {
        service.deleteRoomRate(roomRateId);
        return ResponseEntity.noContent().build();
    }
}
