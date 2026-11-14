package com.hms.controller;

import com.hms.dto.AccommodationDTO;
import com.hms.service.AccommodationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accommodation")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService service;

    @GetMapping
    public String healthCheck() {
        return "Accommodation controller is running";
    }

    @GetMapping("/getselectedaccommodation")
    public ResponseEntity<AccommodationDTO> getSelectedAccommodation(@RequestParam("accommodationId") Long accommodationId) {
        return ResponseEntity.ok(service.getSelectedAccommodation(accommodationId));
    }

    @GetMapping("/getallaccommodations")
    public ResponseEntity<List<AccommodationDTO>> getAllAccommodations() {
        return ResponseEntity.ok(service.getAllAccommodations());
    }

    @PostMapping("/addaccommodation")
    public ResponseEntity<AccommodationDTO> addAccommodation(@Valid @RequestBody AccommodationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAccommodation(dto));
    }

    @PutMapping("/updateaccommodation")
    public ResponseEntity<AccommodationDTO> updateAccommodation(@RequestParam("accommodationId") Long accommodationId, @Valid @RequestBody AccommodationDTO dto) {
        return ResponseEntity.ok(service.updateAccommodation(accommodationId, dto));
    }
}
