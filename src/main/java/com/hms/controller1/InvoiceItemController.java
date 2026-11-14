package com.hms.controller;

import com.hms.dto.InvoiceItemDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.InvoiceItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/invoiceitem")
@RequiredArgsConstructor
@Slf4j
public class InvoiceItemController {

    private final InvoiceItemService invoiceItemService;

    @GetMapping
    public String healthCheck() {
        return invoiceItemService.healthCheck();
    }

    @GetMapping("/getselectedinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> getSelectedInvoiceItem(@RequestParam(name = "invoiceItemId") Long invoiceItemId) {
        if (invoiceItemId == null || invoiceItemId <= 0) return ResponseEntity.badRequest().build();
        try {
            InvoiceItemDTO result = invoiceItemService.getSelectedInvoiceItem(invoiceItemId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve InvoiceItem with ID {}", invoiceItemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallinvoiceitems")
    public ResponseEntity<Page<InvoiceItemDTO>> getAllInvoiceItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(invoiceItemService.getAllInvoiceItems(
                    PageRequest.of(page, size, Sort.by("id").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve InvoiceItem records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> addInvoiceItem(@RequestBody InvoiceItemDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(invoiceItemService.addInvoiceItem(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add InvoiceItem", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> updateInvoiceItem(
            @RequestParam(name = "invoiceItemId") Long invoiceItemId,
            @RequestBody InvoiceItemDTO dto) {
        if (invoiceItemId == null || invoiceItemId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(invoiceItemService.updateInvoiceItem(invoiceItemId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update InvoiceItem with ID {}", invoiceItemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteinvoiceitem")
    public ResponseEntity<Void> deleteInvoiceItem(@RequestParam(name = "invoiceItemId") Long invoiceItemId) {
        if (invoiceItemId == null || invoiceItemId <= 0) return ResponseEntity.badRequest().build();
        try {
            invoiceItemService.deleteInvoiceItem(invoiceItemId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete InvoiceItem with ID {}", invoiceItemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
