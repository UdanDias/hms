package com.hms.controller;

import com.hms.dto.RoomTypeDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/roomtype")
@RequiredArgsConstructor
@Slf4j
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public String healthCheck() {
        return roomTypeService.healthCheck();
    }

    @GetMapping("/getselectedroomtype")
    public ResponseEntity<RoomTypeDTO> getSelectedRoomType(@RequestParam(name = "roomTypeId") Long roomTypeId) {
        if (roomTypeId == null || roomTypeId <= 0) return ResponseEntity.badRequest().build();
        try {
            RoomTypeDTO result = roomTypeService.getSelectedRoomType(roomTypeId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve RoomType with ID {}", roomTypeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroomtypes")
    public ResponseEntity<Page<RoomTypeDTO>> getAllRoomTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(roomTypeService.getAllRoomTypes(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve RoomType records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addroomtype")
    public ResponseEntity<RoomTypeDTO> addRoomType(@RequestBody RoomTypeDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(roomTypeService.addRoomType(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add RoomType", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateroomtype")
    public ResponseEntity<RoomTypeDTO> updateRoomType(
            @RequestParam(name = "roomTypeId") Long roomTypeId,
            @RequestBody RoomTypeDTO dto) {
        if (roomTypeId == null || roomTypeId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(roomTypeService.updateRoomType(roomTypeId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update RoomType with ID {}", roomTypeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteroomtype")
    public ResponseEntity<Void> deleteRoomType(@RequestParam(name = "roomTypeId") Long roomTypeId) {
        if (roomTypeId == null || roomTypeId <= 0) return ResponseEntity.badRequest().build();
        try {
            roomTypeService.deleteRoomType(roomTypeId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete RoomType with ID {}", roomTypeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
