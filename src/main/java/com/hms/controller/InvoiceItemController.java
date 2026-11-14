package com.hms.controller;

import com.hms.dto.InvoiceItemDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.InvoiceItemService;
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
@RequestMapping("/api/v1/invoiceitem")
@RequiredArgsConstructor
@Slf4j
public class InvoiceItemController {

    private final InvoiceItemService invoiceItemService;

    @GetMapping
    public String healthCheck() {
        return "InvoiceItem controller is running";
    }

    @GetMapping("/getselectedinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> getSelectedInvoiceItem(
            @RequestParam(name = "invoiceItemId") Long invoiceItemId) {
        if (invoiceItemId == null || invoiceItemId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            InvoiceItemDTO result = invoiceItemService.getSelectedInvoiceItem(invoiceItemId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve InvoiceItem with ID {}", invoiceItemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallinvoiceitems")
    public ResponseEntity<List<InvoiceItemDTO>> getAllInvoiceItems() {
        try {
            List<InvoiceItemDTO> results = invoiceItemService.getAllInvoiceItems();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve InvoiceItems", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
