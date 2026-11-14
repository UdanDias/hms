package com.hms.controller;

import com.hms.dto.RoomDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomService;
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
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public String healthCheck() {
        return "Room controller is running";
    }

    @GetMapping("/getselectedroom")
    public ResponseEntity<RoomDTO> getSelectedRoom(
            @RequestParam(name = "roomId") Long roomId) {
        if (roomId == null || roomId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RoomDTO result = roomService.getSelectedRoom(roomId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Room with ID {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallrooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        try {
            List<RoomDTO> results = roomService.getAllRooms();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Rooms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
