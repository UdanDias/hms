package com.hms.controller;

import com.hms.dto.ReservationCustomerDTO;
import com.hms.service.ReservationCustomerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservationcustomer")
@RequiredArgsConstructor
public class ReservationCustomerController {

    private final ReservationCustomerService service;

    @GetMapping
    public String healthCheck() {
        return "ReservationCustomer controller is running";
    }

    @GetMapping("/getselectedreservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> getSelectedReservationCustomer(@RequestParam("reservationCustomerId") Long reservationCustomerId) {
        return ResponseEntity.ok(service.getSelectedReservationCustomer(reservationCustomerId));
    }

    @GetMapping("/getallreservationcustomers")
    public ResponseEntity<List<ReservationCustomerDTO>> getAllReservationCustomers() {
        return ResponseEntity.ok(service.getAllReservationCustomers());
    }

    @PostMapping("/addreservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> addReservationCustomer(@Valid @RequestBody ReservationCustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addReservationCustomer(dto));
    }

    @PutMapping("/updatereservationcustomer")
    public ResponseEntity<ReservationCustomerDTO> updateReservationCustomer(@RequestParam("reservationCustomerId") Long reservationCustomerId, @Valid @RequestBody ReservationCustomerDTO dto) {
        return ResponseEntity.ok(service.updateReservationCustomer(reservationCustomerId, dto));
    }

    @DeleteMapping("/deletereservationcustomer")
    public ResponseEntity<Void> deleteReservationCustomer(@RequestParam("reservationCustomerId") Long reservationCustomerId) {
        service.deleteReservationCustomer(reservationCustomerId);
        return ResponseEntity.noContent().build();
    }
}
