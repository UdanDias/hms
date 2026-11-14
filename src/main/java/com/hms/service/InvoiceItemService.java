package com.hms.service;

import com.hms.dto.InvoiceItemDTO;
import java.util.List;

public interface InvoiceItemService {
  InvoiceItemDTO getSelectedInvoiceItem(Long invoiceItemId);

  List<InvoiceItemDTO> getAllInvoiceItems();

  InvoiceItemDTO addInvoiceItem(InvoiceItemDTO dto);

  InvoiceItemDTO updateInvoiceItem(Long invoiceItemId, InvoiceItemDTO dto);

  void deleteInvoiceItem(Long invoiceItemId);
}
