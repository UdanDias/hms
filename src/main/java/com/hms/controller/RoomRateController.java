package com.hms.controller;

import com.hms.dto.RoomRateDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomRateService;
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
@RequestMapping("/api/v1/roomrate")
@RequiredArgsConstructor
@Slf4j
public class RoomRateController {

    private final RoomRateService roomRateService;

    @GetMapping
    public String healthCheck() {
        return "RoomRate controller is running";
    }

    @GetMapping("/getselectedroomrate")
    public ResponseEntity<RoomRateDTO> getSelectedRoomRate(
            @RequestParam(name = "roomRateId") Long roomRateId) {
        if (roomRateId == null || roomRateId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RoomRateDTO result = roomRateService.getSelectedRoomRate(roomRateId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve RoomRate with ID {}", roomRateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroomrates")
    public ResponseEntity<List<RoomRateDTO>> getAllRoomRates() {
        try {
            List<RoomRateDTO> results = roomRateService.getAllRoomRates();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve RoomRates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
