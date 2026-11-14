package com.hms.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefundDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long paymentId;

    private String refundNumber;

    private BigDecimal amount;

    private String refundMethod;

    private String referenceNumber;

    private String reason;

    private String status;

    private LocalDateTime refundedAt;

    private Long processedById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
