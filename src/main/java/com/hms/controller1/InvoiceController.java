package com.hms.controller;

import com.hms.dto.InvoiceDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public String healthCheck() {
        return invoiceService.healthCheck();
    }

    @GetMapping("/getselectedinvoice")
    public ResponseEntity<InvoiceDTO> getSelectedInvoice(@RequestParam(name = "invoiceId") Long invoiceId) {
        if (invoiceId == null || invoiceId <= 0) return ResponseEntity.badRequest().build();
        try {
            InvoiceDTO result = invoiceService.getSelectedInvoice(invoiceId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Invoice with ID {}", invoiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallinvoices")
    public ResponseEntity<Page<InvoiceDTO>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(invoiceService.getAllInvoices(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve Invoice records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addinvoice")
    public ResponseEntity<InvoiceDTO> addInvoice(@RequestBody InvoiceDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.addInvoice(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateinvoice")
    public ResponseEntity<InvoiceDTO> updateInvoice(
            @RequestParam(name = "invoiceId") Long invoiceId,
            @RequestBody InvoiceDTO dto) {
        if (invoiceId == null || invoiceId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(invoiceService.updateInvoice(invoiceId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Invoice with ID {}", invoiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteinvoice")
    public ResponseEntity<Void> deleteInvoice(@RequestParam(name = "invoiceId") Long invoiceId) {
        if (invoiceId == null || invoiceId <= 0) return ResponseEntity.badRequest().build();
        try {
            invoiceService.deleteInvoice(invoiceId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Invoice with ID {}", invoiceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
