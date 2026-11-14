package com.hms.controller;

import com.hms.dto.AccommodationDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.AccommodationService;
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
@RequestMapping("/api/v1/accommodation")
@RequiredArgsConstructor
@Slf4j
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public String healthCheck() {
        return "Accommodation controller is running";
    }

    @GetMapping("/getselectedaccommodation")
    public ResponseEntity<AccommodationDTO> getSelectedAccommodation(
            @RequestParam(name = "accommodationId") Long accommodationId) {
        if (accommodationId == null || accommodationId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AccommodationDTO result = accommodationService.getSelectedAccommodation(accommodationId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Accommodation with ID {}", accommodationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallaccommodations")
    public ResponseEntity<List<AccommodationDTO>> getAllAccommodations() {
        try {
            List<AccommodationDTO> results = accommodationService.getAllAccommodations();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Accommodations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
