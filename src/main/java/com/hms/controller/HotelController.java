package com.hms.controller;

import com.hms.dto.HotelDTO;
import com.hms.service.HotelService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hotel")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService service;

    @GetMapping
    public String healthCheck() {
        return "Hotel controller is running";
    }

    @GetMapping("/getselectedhotel")
    public ResponseEntity<HotelDTO> getSelectedHotel(@RequestParam("hotelId") Long hotelId) {
        return ResponseEntity.ok(service.getSelectedHotel(hotelId));
    }

    @GetMapping("/getallhotels")
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        return ResponseEntity.ok(service.getAllHotels());
    }

    @PostMapping("/addhotel")
    public ResponseEntity<HotelDTO> addHotel(@Valid @RequestBody HotelDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addHotel(dto));
    }

    @PutMapping("/updatehotel")
    public ResponseEntity<HotelDTO> updateHotel(@RequestParam("hotelId") Long hotelId, @Valid @RequestBody HotelDTO dto) {
        return ResponseEntity.ok(service.updateHotel(hotelId, dto));
    }
}
