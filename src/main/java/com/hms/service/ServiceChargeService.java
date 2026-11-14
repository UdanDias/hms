package com.hms.service;

import com.hms.dto.ServiceChargeDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface ServiceChargeService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    ServiceChargeDTO getSelectedServiceCharge(Long serviceChargeId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<ServiceChargeDTO> getAllServiceCharges();
}
