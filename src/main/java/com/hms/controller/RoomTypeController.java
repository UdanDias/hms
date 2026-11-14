package com.hms.controller;

import com.hms.dto.RoomTypeDTO;
import com.hms.service.RoomTypeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roomtype")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService service;

    @GetMapping
    public String healthCheck() {
        return "RoomType controller is running";
    }

    @GetMapping("/getselectedroomtype")
    public ResponseEntity<RoomTypeDTO> getSelectedRoomType(@RequestParam("roomTypeId") Long roomTypeId) {
        return ResponseEntity.ok(service.getSelectedRoomType(roomTypeId));
    }

    @GetMapping("/getallroomtypes")
    public ResponseEntity<List<RoomTypeDTO>> getAllRoomTypes() {
        return ResponseEntity.ok(service.getAllRoomTypes());
    }

    @PostMapping("/addroomtype")
    public ResponseEntity<RoomTypeDTO> addRoomType(@Valid @RequestBody RoomTypeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRoomType(dto));
    }

    @PutMapping("/updateroomtype")
    public ResponseEntity<RoomTypeDTO> updateRoomType(@RequestParam("roomTypeId") Long roomTypeId, @Valid @RequestBody RoomTypeDTO dto) {
        return ResponseEntity.ok(service.updateRoomType(roomTypeId, dto));
    }
}
