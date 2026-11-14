package com.hms.controller;

import com.hms.dto.ReservationCustomerDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/reservationcustomer")
@RequiredArgsConstructor
@Slf4j
public class ReservationCustomerController {

    private final ReservationCustomerService reservationCustomerService;

    @GetMapping
    public String healthCheck() {
        return reservationCustomerService.healthCheck();
    }

    @GetMapping("/getselectedreservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> getSelectedReservationCustomer(@RequestParam(name = "reservationCustomerId") Long reservationCustomerId) {
        if (reservationCustomerId == null || reservationCustomerId <= 0) return ResponseEntity.badRequest().build();
        try {
            ReservationCustomerDTO result = reservationCustomerService.getSelectedReservationCustomer(reservationCustomerId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationCustomer with ID {}", reservationCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreservationcustomers")
    public ResponseEntity<Page<ReservationCustomerDTO>> getAllReservationCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(reservationCustomerService.getAllReservationCustomers(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationCustomer records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addreservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> addReservationCustomer(@RequestBody ReservationCustomerDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationCustomerService.addReservationCustomer(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add ReservationCustomer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updatereservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> updateReservationCustomer(
            @RequestParam(name = "reservationCustomerId") Long reservationCustomerId,
            @RequestBody ReservationCustomerDTO dto) {
        if (reservationCustomerId == null || reservationCustomerId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(reservationCustomerService.updateReservationCustomer(reservationCustomerId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update ReservationCustomer with ID {}", reservationCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletereservationcustomer")
    public ResponseEntity<Void> deleteReservationCustomer(@RequestParam(name = "reservationCustomerId") Long reservationCustomerId) {
        if (reservationCustomerId == null || reservationCustomerId <= 0) return ResponseEntity.badRequest().build();
        try {
            reservationCustomerService.deleteReservationCustomer(reservationCustomerId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete ReservationCustomer with ID {}", reservationCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
