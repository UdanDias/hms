package com.hms.controller;

import com.hms.dto.InvoiceItemDTO;
import com.hms.service.InvoiceItemService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoiceitem")
@RequiredArgsConstructor
public class InvoiceItemController {

    private final InvoiceItemService service;

    @GetMapping
    public String healthCheck() {
        return "InvoiceItem controller is running";
    }

    @GetMapping("/getselectedinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> getSelectedInvoiceItem(@RequestParam("invoiceItemId") Long invoiceItemId) {
        return ResponseEntity.ok(service.getSelectedInvoiceItem(invoiceItemId));
    }

    @GetMapping("/getallinvoiceitems")
    public ResponseEntity<List<InvoiceItemDTO>> getAllInvoiceItems() {
        return ResponseEntity.ok(service.getAllInvoiceItems());
    }

    @PostMapping("/addinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> addInvoiceItem(@Valid @RequestBody InvoiceItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addInvoiceItem(dto));
    }

    @PutMapping("/updateinvoiceitem")
    public ResponseEntity<InvoiceItemDTO> updateInvoiceItem(@RequestParam("invoiceItemId") Long invoiceItemId, @Valid @RequestBody InvoiceItemDTO dto) {
        return ResponseEntity.ok(service.updateInvoiceItem(invoiceItemId, dto));
    }

    @DeleteMapping("/deleteinvoiceitem")
    public ResponseEntity<Void> deleteInvoiceItem(@RequestParam("invoiceItemId") Long invoiceItemId) {
        service.deleteInvoiceItem(invoiceItemId);
        return ResponseEntity.noContent().build();
    }
}
