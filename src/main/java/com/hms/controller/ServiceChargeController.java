package com.hms.controller;

import com.hms.dto.ServiceChargeDTO;
import com.hms.service.ServiceChargeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/servicecharge")
@RequiredArgsConstructor
public class ServiceChargeController {

    private final ServiceChargeService service;

    @GetMapping
    public String healthCheck() {
        return "ServiceCharge controller is running";
    }

    @GetMapping("/getselectedservicecharge")
    public ResponseEntity<ServiceChargeDTO> getSelectedServiceCharge(@RequestParam("serviceChargeId") Long serviceChargeId) {
        return ResponseEntity.ok(service.getSelectedServiceCharge(serviceChargeId));
    }

    @GetMapping("/getallservicecharges")
    public ResponseEntity<List<ServiceChargeDTO>> getAllServiceCharges() {
        return ResponseEntity.ok(service.getAllServiceCharges());
    }

    @PostMapping("/addservicecharge")
    public ResponseEntity<ServiceChargeDTO> addServiceCharge(@Valid @RequestBody ServiceChargeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addServiceCharge(dto));
    }

    @PutMapping("/updateservicecharge")
    public ResponseEntity<ServiceChargeDTO> updateServiceCharge(@RequestParam("serviceChargeId") Long serviceChargeId, @Valid @RequestBody ServiceChargeDTO dto) {
        return ResponseEntity.ok(service.updateServiceCharge(serviceChargeId, dto));
    }

    @PatchMapping("/voidservicecharge")
    public ResponseEntity<ServiceChargeDTO> voidServiceCharge(@RequestParam("serviceChargeId") Long serviceChargeId) {
        return ResponseEntity.ok(service.voidServiceCharge(serviceChargeId));
    }
}
