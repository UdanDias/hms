package com.hms.service;

import com.hms.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
  PaymentDTO getSelectedPayment(Long paymentId);
  Page<PaymentDTO> getAllPayments(Pageable pageable);

  PaymentDTO addPayment(PaymentDTO dto);

  PaymentDTO allocatePayment(Long paymentId, Long invoiceId);

  default String healthCheck() { return "Payment service is running"; }

  PaymentDTO updatePayment(Long id, PaymentDTO dto);

  void deletePayment(Long id);
}
