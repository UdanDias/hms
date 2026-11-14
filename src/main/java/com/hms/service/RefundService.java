package com.hms.service;

import com.hms.dto.RefundDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface RefundService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    RefundDTO getSelectedRefund(Long refundId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<RefundDTO> getAllRefunds();
}
