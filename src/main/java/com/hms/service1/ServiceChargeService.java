package com.hms.service;

import com.hms.dto.ServiceChargeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceChargeService {
  ServiceChargeDTO getSelectedServiceCharge(Long serviceChargeId);
  Page<ServiceChargeDTO> getAllServiceCharges(Pageable pageable);

  ServiceChargeDTO addServiceCharge(ServiceChargeDTO dto);

  ServiceChargeDTO updateServiceCharge(Long id, ServiceChargeDTO dto);

  ServiceChargeDTO voidServiceCharge(Long serviceChargeId);

  default String healthCheck() { return "ServiceCharge service is running"; }

  void deleteServiceCharge(Long id);
}
