package com.hms.service;

import com.hms.dto.PaymentDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface PaymentService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    PaymentDTO getSelectedPayment(Long paymentId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<PaymentDTO> getAllPayments();
}
