package com.hms.service;

import com.hms.dto.ServiceChargeDTO;
import java.util.List;

public interface ServiceChargeService {
  ServiceChargeDTO getSelectedServiceCharge(Long serviceChargeId);

  List<ServiceChargeDTO> getAllServiceCharges();

  ServiceChargeDTO addServiceCharge(ServiceChargeDTO dto);

  ServiceChargeDTO updateServiceCharge(Long id, ServiceChargeDTO dto);

  ServiceChargeDTO voidServiceCharge(Long serviceChargeId);
}
