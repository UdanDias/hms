package com.hms.service;

import com.hms.dto.RefundDTO;
import java.util.List;

public interface RefundService {
  RefundDTO getSelectedRefund(Long refundId);

  List<RefundDTO> getAllRefunds();

  RefundDTO addRefund(RefundDTO dto);
}
