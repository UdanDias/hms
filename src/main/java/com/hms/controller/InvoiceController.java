package com.hms.controller;

import com.hms.dto.InvoiceDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.InvoiceService;
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
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public String healthCheck() {
        return "Invoice controller is running";
    }

    @GetMapping("/getselectedinvoice")
    public ResponseEntity<InvoiceDTO> getSelectedInvoice(
            @RequestParam(name = "invoiceId") Long invoiceId) {
        if (invoiceId == null || invoiceId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            InvoiceDTO result = invoiceService.getSelectedInvoice(invoiceId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Invoice with ID {}", invoiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallinvoices")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        try {
            List<InvoiceDTO> results = invoiceService.getAllInvoices();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Invoices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
