package com.hms.service;

import com.hms.dto.InvoiceItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceItemService {
  InvoiceItemDTO getSelectedInvoiceItem(Long invoiceItemId);
  Page<InvoiceItemDTO> getAllInvoiceItems(Pageable pageable);

  InvoiceItemDTO addInvoiceItem(InvoiceItemDTO dto);

  InvoiceItemDTO updateInvoiceItem(Long id, InvoiceItemDTO dto);

  void deleteInvoiceItem(Long id);

  default String healthCheck() { return "InvoiceItem service is running"; }
}
