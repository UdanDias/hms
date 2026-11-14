package com.hms.service;

import com.hms.dto.InvoiceDTO;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
  InvoiceDTO getSelectedInvoice(Long invoiceId);
  Page<InvoiceDTO> getAllInvoices(Pageable pageable);

  InvoiceDTO addInvoice(InvoiceDTO dto);

  InvoiceDTO updateInvoice(Long id, InvoiceDTO dto);

  InvoiceDTO issueInvoice(Long invoiceId);

  InvoiceDTO voidInvoice(Long invoiceId);

  BigDecimal getOutstandingBalance(Long invoiceId);

  default String healthCheck() { return "Invoice service is running"; }

  void deleteInvoice(Long id);
}
