package com.hms.controller;

import com.hms.dto.HotelServiceDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.HotelServiceManagementService;
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
@RequestMapping("/api/v1/hotelservice")
@RequiredArgsConstructor
@Slf4j
public class HotelServiceController {

    private final HotelServiceManagementService hotelServiceManagementService;

    @GetMapping
    public String healthCheck() {
        return "HotelService controller is running";
    }

    @GetMapping("/getselectedhotelservice")
    public ResponseEntity<HotelServiceDTO> getSelectedHotelService(
            @RequestParam(name = "hotelServiceId") Long hotelServiceId) {
        if (hotelServiceId == null || hotelServiceId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            HotelServiceDTO result = hotelServiceManagementService.getSelectedHotelService(hotelServiceId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve HotelService with ID {}", hotelServiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallhotelservices")
    public ResponseEntity<List<HotelServiceDTO>> getAllHotelServices() {
        try {
            List<HotelServiceDTO> results = hotelServiceManagementService.getAllHotelServices();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve HotelServices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
