package com.hms.service;

import com.hms.dto.InvoiceItemDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface InvoiceItemService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    InvoiceItemDTO getSelectedInvoiceItem(Long invoiceItemId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<InvoiceItemDTO> getAllInvoiceItems();
}
