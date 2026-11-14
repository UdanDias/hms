package com.hms.controller;

import com.hms.dto.HotelServiceDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.HotelServiceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/hotelservice")
@RequiredArgsConstructor
@Slf4j
public class HotelServiceController {

    private final HotelServiceManagementService hotelServiceManagementService;

    @GetMapping
    public String healthCheck() {
        return hotelServiceManagementService.healthCheck();
    }

    @GetMapping("/getselectedhotelservice")
    public ResponseEntity<HotelServiceDTO> getSelectedHotelService(@RequestParam(name = "hotelServiceId") Long hotelServiceId) {
        if (hotelServiceId == null || hotelServiceId <= 0) return ResponseEntity.badRequest().build();
        try {
            HotelServiceDTO result = hotelServiceManagementService.getSelectedHotelService(hotelServiceId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve HotelService with ID {}", hotelServiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallhotelservices")
    public ResponseEntity<Page<HotelServiceDTO>> getAllHotelServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(hotelServiceManagementService.getAllHotelServices(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve HotelService records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addhotelservice")
    public ResponseEntity<HotelServiceDTO> addHotelService(@RequestBody HotelServiceDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(hotelServiceManagementService.addHotelService(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add HotelService", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updatehotelservice")
    public ResponseEntity<HotelServiceDTO> updateHotelService(
            @RequestParam(name = "hotelServiceId") Long hotelServiceId,
            @RequestBody HotelServiceDTO dto) {
        if (hotelServiceId == null || hotelServiceId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(hotelServiceManagementService.updateHotelService(hotelServiceId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update HotelService with ID {}", hotelServiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletehotelservice")
    public ResponseEntity<Void> deleteHotelService(@RequestParam(name = "hotelServiceId") Long hotelServiceId) {
        if (hotelServiceId == null || hotelServiceId <= 0) return ResponseEntity.badRequest().build();
        try {
            hotelServiceManagementService.deleteHotelService(hotelServiceId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete HotelService with ID {}", hotelServiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
