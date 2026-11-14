package com.hms.controller;

import com.hms.dto.HotelServiceDTO;
import com.hms.service.HotelServiceManagementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hotelservice")
@RequiredArgsConstructor
public class HotelServiceController {

    private final HotelServiceManagementService service;

    @GetMapping
    public String healthCheck() {
        return "HotelService controller is running";
    }

    @GetMapping("/getselectedhotelservice")
    public ResponseEntity<HotelServiceDTO> getSelectedHotelService(@RequestParam("hotelServiceId") Long hotelServiceId) {
        return ResponseEntity.ok(service.getSelectedHotelService(hotelServiceId));
    }

    @GetMapping("/getallhotelservices")
    public ResponseEntity<List<HotelServiceDTO>> getAllHotelServices() {
        return ResponseEntity.ok(service.getAllHotelServices());
    }

    @PostMapping("/addhotelservice")
    public ResponseEntity<HotelServiceDTO> addHotelService(@Valid @RequestBody HotelServiceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addHotelService(dto));
    }

    @PutMapping("/updatehotelservice")
    public ResponseEntity<HotelServiceDTO> updateHotelService(@RequestParam("hotelServiceId") Long hotelServiceId, @Valid @RequestBody HotelServiceDTO dto) {
        return ResponseEntity.ok(service.updateHotelService(hotelServiceId, dto));
    }
}
