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
public class InvoiceItemDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long invoiceId;

    private Long reservationRoomId;

    private Long serviceChargeId;

    private String description;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private BigDecimal taxRate;

    private BigDecimal taxAmount;

    private BigDecimal serviceChargeAmount;

    private BigDecimal lineTotal;

    private Long accommodationId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
