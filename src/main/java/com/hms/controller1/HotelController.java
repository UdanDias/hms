package com.hms.controller;

import com.hms.dto.HotelDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hotel")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public String healthCheck() {
        return hotelService.healthCheck();
    }

    @GetMapping("/getselectedhotel")
    public ResponseEntity<HotelDTO> getSelectedHotel(@RequestParam(name = "hotelId") Long hotelId) {
        if (hotelId == null || hotelId <= 0) return ResponseEntity.badRequest().build();
        try {
            HotelDTO result = hotelService.getSelectedHotel(hotelId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Hotel with ID {}", hotelId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallhotels")
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        try {
            return ResponseEntity.ok(hotelService.getAllHotels());
        } catch (Exception e) {
            log.error("Failed to retrieve Hotel records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addhotel")
    public ResponseEntity<HotelDTO> addHotel(@RequestBody HotelDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.addHotel(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Hotel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updatehotel")
    public ResponseEntity<HotelDTO> updateHotel(
            @RequestParam(name = "hotelId") Long hotelId,
            @RequestBody HotelDTO dto) {
        if (hotelId == null || hotelId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(hotelService.updateHotel(hotelId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Hotel with ID {}", hotelId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletehotel")
    public ResponseEntity<Void> deleteHotel(@RequestParam(name = "hotelId") Long hotelId) {
        if (hotelId == null || hotelId <= 0) return ResponseEntity.badRequest().build();
        try {
            hotelService.deleteHotel(hotelId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Hotel with ID {}", hotelId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
