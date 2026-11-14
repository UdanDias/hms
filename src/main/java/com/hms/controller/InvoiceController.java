package com.hms.controller;

import com.hms.dto.InvoiceDTO;
import com.hms.service.InvoiceService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @GetMapping
    public String healthCheck() {
        return "Invoice controller is running";
    }

    @GetMapping("/getselectedinvoice")
    public ResponseEntity<InvoiceDTO> getSelectedInvoice(@RequestParam("invoiceId") Long invoiceId) {
        return ResponseEntity.ok(service.getSelectedInvoice(invoiceId));
    }

    @GetMapping("/getallinvoices")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        return ResponseEntity.ok(service.getAllInvoices());
    }

    @PostMapping("/addinvoice")
    public ResponseEntity<InvoiceDTO> addInvoice(@Valid @RequestBody InvoiceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addInvoice(dto));
    }

    @PutMapping("/updateinvoice")
    public ResponseEntity<InvoiceDTO> updateInvoice(@RequestParam("invoiceId") Long invoiceId, @Valid @RequestBody InvoiceDTO dto) {
        return ResponseEntity.ok(service.updateInvoice(invoiceId, dto));
    }

    @PatchMapping("/issueinvoice")
    public ResponseEntity<InvoiceDTO> issueInvoice(@RequestParam("invoiceId") Long invoiceId) {
        return ResponseEntity.ok(service.issueInvoice(invoiceId));
    }

    @PatchMapping("/voidinvoice")
    public ResponseEntity<InvoiceDTO> voidInvoice(@RequestParam("invoiceId") Long invoiceId) {
        return ResponseEntity.ok(service.voidInvoice(invoiceId));
    }

    @GetMapping("/getoutstandingbalance")
    public ResponseEntity<BigDecimal> getOutstandingBalance(@RequestParam("invoiceId") Long invoiceId) {
        return ResponseEntity.ok(service.getOutstandingBalance(invoiceId));
    }
}
