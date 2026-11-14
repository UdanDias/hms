package com.hms.controller;

import com.hms.dto.ServiceChargeDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ServiceChargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/servicecharge")
@RequiredArgsConstructor
@Slf4j
public class ServiceChargeController {

    private final ServiceChargeService serviceChargeService;

    @GetMapping
    public String healthCheck() {
        return serviceChargeService.healthCheck();
    }

    @GetMapping("/getselectedservicecharge")
    public ResponseEntity<ServiceChargeDTO> getSelectedServiceCharge(@RequestParam(name = "serviceChargeId") Long serviceChargeId) {
        if (serviceChargeId == null || serviceChargeId <= 0) return ResponseEntity.badRequest().build();
        try {
            ServiceChargeDTO result = serviceChargeService.getSelectedServiceCharge(serviceChargeId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve ServiceCharge with ID {}", serviceChargeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallservicecharges")
    public ResponseEntity<Page<ServiceChargeDTO>> getAllServiceCharges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(serviceChargeService.getAllServiceCharges(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve ServiceCharge records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addservicecharge")
    public ResponseEntity<ServiceChargeDTO> addServiceCharge(@RequestBody ServiceChargeDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(serviceChargeService.addServiceCharge(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add ServiceCharge", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateservicecharge")
    public ResponseEntity<ServiceChargeDTO> updateServiceCharge(
            @RequestParam(name = "serviceChargeId") Long serviceChargeId,
            @RequestBody ServiceChargeDTO dto) {
        if (serviceChargeId == null || serviceChargeId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(serviceChargeService.updateServiceCharge(serviceChargeId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update ServiceCharge with ID {}", serviceChargeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteservicecharge")
    public ResponseEntity<Void> deleteServiceCharge(@RequestParam(name = "serviceChargeId") Long serviceChargeId) {
        if (serviceChargeId == null || serviceChargeId <= 0) return ResponseEntity.badRequest().build();
        try {
            serviceChargeService.deleteServiceCharge(serviceChargeId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete ServiceCharge with ID {}", serviceChargeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
