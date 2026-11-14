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
public class PaymentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long hotelId;

    private Long reservationId;

    private Long invoiceId;

    private String receiptNumber;

    private BigDecimal amount;

    private String currencyCode;

    private String paymentMethod;

    private String referenceNumber;

    private LocalDateTime paidAt;

    private String status;

    private Long receivedById;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
