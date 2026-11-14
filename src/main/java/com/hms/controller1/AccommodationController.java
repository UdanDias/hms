package com.hms.controller;

import com.hms.dto.AccommodationDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.AccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/accommodation")
@RequiredArgsConstructor
@Slf4j
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public String healthCheck() {
        return accommodationService.healthCheck();
    }

    @GetMapping("/getselectedaccommodation")
    public ResponseEntity<AccommodationDTO> getSelectedAccommodation(@RequestParam(name = "accommodationId") Long accommodationId) {
        if (accommodationId == null || accommodationId <= 0) return ResponseEntity.badRequest().build();
        try {
            AccommodationDTO result = accommodationService.getSelectedAccommodation(accommodationId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Accommodation with ID {}", accommodationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallaccommodations")
    public ResponseEntity<Page<AccommodationDTO>> getAllAccommodations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(accommodationService.getAllAccommodations(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve Accommodation records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addaccommodation")
    public ResponseEntity<AccommodationDTO> addAccommodation(@RequestBody AccommodationDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(accommodationService.addAccommodation(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Accommodation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateaccommodation")
    public ResponseEntity<AccommodationDTO> updateAccommodation(
            @RequestParam(name = "accommodationId") Long accommodationId,
            @RequestBody AccommodationDTO dto) {
        if (accommodationId == null || accommodationId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(accommodationService.updateAccommodation(accommodationId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Accommodation with ID {}", accommodationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteaccommodation")
    public ResponseEntity<Void> deleteAccommodation(@RequestParam(name = "accommodationId") Long accommodationId) {
        if (accommodationId == null || accommodationId <= 0) return ResponseEntity.badRequest().build();
        try {
            accommodationService.deleteAccommodation(accommodationId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Accommodation with ID {}", accommodationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
