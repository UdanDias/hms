package com.hms.service;

import com.hms.dto.RefundDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundService {
  RefundDTO getSelectedRefund(Long refundId);
  Page<RefundDTO> getAllRefunds(Pageable pageable);

  RefundDTO addRefund(RefundDTO dto);

  default String healthCheck() { return "Refund service is running"; }

  RefundDTO updateRefund(Long id, RefundDTO dto);

  void deleteRefund(Long id);
}
