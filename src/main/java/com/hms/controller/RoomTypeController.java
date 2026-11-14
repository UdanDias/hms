package com.hms.controller;

import com.hms.dto.RoomTypeDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomTypeService;
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
@RequestMapping("/api/v1/roomtype")
@RequiredArgsConstructor
@Slf4j
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public String healthCheck() {
        return "RoomType controller is running";
    }

    @GetMapping("/getselectedroomtype")
    public ResponseEntity<RoomTypeDTO> getSelectedRoomType(
            @RequestParam(name = "roomTypeId") Long roomTypeId) {
        if (roomTypeId == null || roomTypeId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RoomTypeDTO result = roomTypeService.getSelectedRoomType(roomTypeId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve RoomType with ID {}", roomTypeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroomtypes")
    public ResponseEntity<List<RoomTypeDTO>> getAllRoomTypes() {
        try {
            List<RoomTypeDTO> results = roomTypeService.getAllRoomTypes();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve RoomTypes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
