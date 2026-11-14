package com.hms.controller;

import com.hms.dto.RoomDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public String healthCheck() {
        return roomService.healthCheck();
    }

    @GetMapping("/getselectedroom")
    public ResponseEntity<RoomDTO> getSelectedRoom(@RequestParam(name = "roomId") Long roomId) {
        if (roomId == null || roomId <= 0) return ResponseEntity.badRequest().build();
        try {
            RoomDTO result = roomService.getSelectedRoom(roomId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Room with ID {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallrooms")
    public ResponseEntity<Page<RoomDTO>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(roomService.getAllRooms(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve Room records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addroom")
    public ResponseEntity<RoomDTO> addRoom(@RequestBody RoomDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(roomService.addRoom(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateroom")
    public ResponseEntity<RoomDTO> updateRoom(
            @RequestParam(name = "roomId") Long roomId,
            @RequestBody RoomDTO dto) {
        if (roomId == null || roomId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(roomService.updateRoom(roomId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Room with ID {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteroom")
    public ResponseEntity<Void> deleteRoom(@RequestParam(name = "roomId") Long roomId) {
        if (roomId == null || roomId <= 0) return ResponseEntity.badRequest().build();
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Room with ID {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
