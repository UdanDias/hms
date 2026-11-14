package com.hms.service;

import com.hms.dto.PaymentDTO;
import java.util.List;

public interface PaymentService {
  PaymentDTO getSelectedPayment(Long paymentId);

  List<PaymentDTO> getAllPayments();

  PaymentDTO addPayment(PaymentDTO dto);

  PaymentDTO allocatePayment(Long paymentId, Long invoiceId);
}
