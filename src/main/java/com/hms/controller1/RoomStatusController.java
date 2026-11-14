package com.hms.controller;

import com.hms.dto.RoomStatusDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/roomstatus")
@RequiredArgsConstructor
@Slf4j
public class RoomStatusController {

    private final RoomStatusService roomStatusService;

    @GetMapping
    public String healthCheck() {
        return roomStatusService.healthCheck();
    }

    @GetMapping("/getselectedroomstatus")
    public ResponseEntity<RoomStatusDTO> getSelectedRoomStatus(@RequestParam(name = "roomStatusId") Long roomStatusId) {
        if (roomStatusId == null || roomStatusId <= 0) return ResponseEntity.badRequest().build();
        try {
            RoomStatusDTO result = roomStatusService.getSelectedRoomStatus(roomStatusId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve RoomStatus with ID {}", roomStatusId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroomstatuses")
    public ResponseEntity<Page<RoomStatusDTO>> getAllRoomStatuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(roomStatusService.getAllRoomStatuses(
                    PageRequest.of(page, size, Sort.by("statusId").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve RoomStatus records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addroomstatus")
    public ResponseEntity<RoomStatusDTO> addRoomStatus(@RequestBody RoomStatusDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(roomStatusService.addRoomStatus(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add RoomStatus", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateroomstatus")
    public ResponseEntity<RoomStatusDTO> updateRoomStatus(
            @RequestParam(name = "roomStatusId") Long roomStatusId,
            @RequestBody RoomStatusDTO dto) {
        if (roomStatusId == null || roomStatusId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(roomStatusService.updateRoomStatus(roomStatusId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update RoomStatus with ID {}", roomStatusId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteroomstatus")
    public ResponseEntity<Void> deleteRoomStatus(@RequestParam(name = "roomStatusId") Long roomStatusId) {
        if (roomStatusId == null || roomStatusId <= 0) return ResponseEntity.badRequest().build();
        try {
            roomStatusService.deleteRoomStatus(roomStatusId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete RoomStatus with ID {}", roomStatusId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
