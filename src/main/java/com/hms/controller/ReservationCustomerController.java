package com.hms.controller;

import com.hms.dto.ReservationCustomerDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.ReservationCustomerService;
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
@RequestMapping("/api/v1/reservationcustomer")
@RequiredArgsConstructor
@Slf4j
public class ReservationCustomerController {

    private final ReservationCustomerService reservationCustomerService;

    @GetMapping
    public String healthCheck() {
        return "ReservationCustomer controller is running";
    }

    @GetMapping("/getselectedreservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> getSelectedReservationCustomer(
            @RequestParam(name = "reservationCustomerId") Long reservationCustomerId) {
        if (reservationCustomerId == null || reservationCustomerId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ReservationCustomerDTO result = reservationCustomerService.getSelectedReservationCustomer(reservationCustomerId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationCustomer with ID {}", reservationCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallreservationcustomers")
    public ResponseEntity<List<ReservationCustomerDTO>> getAllReservationCustomers() {
        try {
            List<ReservationCustomerDTO> results = reservationCustomerService.getAllReservationCustomers();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve ReservationCustomers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
