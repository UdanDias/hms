package com.hms.controller;

import com.hms.dto.HotelDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.HotelService;
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
@RequestMapping("/api/v1/hotel")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public String healthCheck() {
        return "Hotel controller is running";
    }

    @GetMapping("/getselectedhotel")
    public ResponseEntity<HotelDTO> getSelectedHotel(
            @RequestParam(name = "hotelId") Long hotelId) {
        if (hotelId == null || hotelId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            HotelDTO result = hotelService.getSelectedHotel(hotelId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
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
            List<HotelDTO> results = hotelService.getAllHotels();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Hotels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
