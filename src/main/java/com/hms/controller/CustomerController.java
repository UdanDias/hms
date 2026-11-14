package com.hms.controller;

import com.hms.dto.CustomerDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.CustomerService;
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
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String healthCheck() {
        return "Customer controller is running";
    }

    @GetMapping("/getselectedcustomer")
    public ResponseEntity<CustomerDTO> getSelectedCustomer(
            @RequestParam(name = "customerId") Long customerId) {
        if (customerId == null || customerId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            CustomerDTO result = customerService.getSelectedCustomer(customerId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Customer with ID {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallcustomers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        try {
            List<CustomerDTO> results = customerService.getAllCustomers();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Customers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
