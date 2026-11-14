package com.hms.controller;

import com.hms.dto.RoomStatusDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomStatusService;
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
@RequestMapping("/api/v1/roomstatus")
@RequiredArgsConstructor
@Slf4j
public class RoomStatusController {

    private final RoomStatusService roomStatusService;

    @GetMapping
    public String healthCheck() {
        return "RoomStatus controller is running";
    }

    @GetMapping("/getselectedroomstatus")
    public ResponseEntity<RoomStatusDTO> getSelectedRoomStatus(
            @RequestParam(name = "roomStatusId") Long roomStatusId) {
        if (roomStatusId == null || roomStatusId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RoomStatusDTO result = roomStatusService.getSelectedRoomStatus(roomStatusId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve RoomStatus with ID {}", roomStatusId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroomstatuses")
    public ResponseEntity<List<RoomStatusDTO>> getAllRoomStatuses() {
        try {
            List<RoomStatusDTO> results = roomStatusService.getAllRoomStatuses();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve RoomStatuses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
