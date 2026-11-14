package com.hms.service;

import com.hms.dto.InvoiceDTO;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {
  InvoiceDTO getSelectedInvoice(Long invoiceId);

  List<InvoiceDTO> getAllInvoices();

  InvoiceDTO addInvoice(InvoiceDTO dto);

  InvoiceDTO updateInvoice(Long id, InvoiceDTO dto);

  InvoiceDTO issueInvoice(Long invoiceId);

  InvoiceDTO voidInvoice(Long invoiceId);

  BigDecimal getOutstandingBalance(Long invoiceId);
}
