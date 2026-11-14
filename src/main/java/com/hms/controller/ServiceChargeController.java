package com.hms.controller;

import com.hms.dto.ServiceChargeDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ServiceChargeService;
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
@RequestMapping("/api/v1/servicecharge")
@RequiredArgsConstructor
@Slf4j
public class ServiceChargeController {

    private final ServiceChargeService serviceChargeService;

    @GetMapping
    public String healthCheck() {
        return "ServiceCharge controller is running";
    }

    @GetMapping("/getselectedservicecharge")
    public ResponseEntity<ServiceChargeDTO> getSelectedServiceCharge(
            @RequestParam(name = "serviceChargeId") Long serviceChargeId) {
        if (serviceChargeId == null || serviceChargeId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ServiceChargeDTO result = serviceChargeService.getSelectedServiceCharge(serviceChargeId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve ServiceCharge with ID {}", serviceChargeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallservicecharges")
    public ResponseEntity<List<ServiceChargeDTO>> getAllServiceCharges() {
        try {
            List<ServiceChargeDTO> results = serviceChargeService.getAllServiceCharges();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve ServiceCharges", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
