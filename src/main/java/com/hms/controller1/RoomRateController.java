package com.hms.controller;

import com.hms.dto.RoomRateDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/roomrate")
@RequiredArgsConstructor
@Slf4j
public class RoomRateController {

    private final RoomRateService roomRateService;

    @GetMapping
    public String healthCheck() {
        return roomRateService.healthCheck();
    }

    @GetMapping("/getselectedroomrate")
    public ResponseEntity<RoomRateDTO> getSelectedRoomRate(@RequestParam(name = "roomRateId") Long roomRateId) {
        if (roomRateId == null || roomRateId <= 0) return ResponseEntity.badRequest().build();
        try {
            RoomRateDTO result = roomRateService.getSelectedRoomRate(roomRateId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve RoomRate with ID {}", roomRateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroomrates")
    public ResponseEntity<Page<RoomRateDTO>> getAllRoomRates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(roomRateService.getAllRoomRates(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve RoomRate records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addroomrate")
    public ResponseEntity<RoomRateDTO> addRoomRate(@RequestBody RoomRateDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(roomRateService.addRoomRate(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add RoomRate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateroomrate")
    public ResponseEntity<RoomRateDTO> updateRoomRate(
            @RequestParam(name = "roomRateId") Long roomRateId,
            @RequestBody RoomRateDTO dto) {
        if (roomRateId == null || roomRateId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(roomRateService.updateRoomRate(roomRateId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update RoomRate with ID {}", roomRateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteroomrate")
    public ResponseEntity<Void> deleteRoomRate(@RequestParam(name = "roomRateId") Long roomRateId) {
        if (roomRateId == null || roomRateId <= 0) return ResponseEntity.badRequest().build();
        try {
            roomRateService.deleteRoomRate(roomRateId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete RoomRate with ID {}", roomRateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
