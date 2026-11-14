package com.hms.service;

import com.hms.dto.InvoiceDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface InvoiceService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    InvoiceDTO getSelectedInvoice(Long invoiceId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<InvoiceDTO> getAllInvoices();
}
