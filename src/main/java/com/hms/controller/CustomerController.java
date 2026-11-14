package com.hms.controller;

import com.hms.dto.CustomerDTO;
import com.hms.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @GetMapping
    public String healthCheck() {
        return "Customer controller is running";
    }

    @GetMapping("/getselectedcustomer")
    public ResponseEntity<CustomerDTO> getSelectedCustomer(@RequestParam("customerId") Long customerId) {
        return ResponseEntity.ok(service.getSelectedCustomer(customerId));
    }

    @GetMapping("/getallcustomers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(service.getAllCustomers());
    }

    @PostMapping("/addcustomer")
    public ResponseEntity<CustomerDTO> addCustomer(@Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addCustomer(dto));
    }

    @PutMapping("/updatecustomer")
    public ResponseEntity<CustomerDTO> updateCustomer(@RequestParam("customerId") Long customerId, @Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(service.updateCustomer(customerId, dto));
    }

    @DeleteMapping("/deletecustomer")
    public ResponseEntity<Void> deleteCustomer(@RequestParam("customerId") Long customerId) {
        service.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
