package com.hms.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long hotelId;

    private Long reservationId;

    private String invoiceNumber;

    private Long billedCustomerId;

    private String billingNameSnapshot;

    private String billingAddressSnapshot;

    private String currencyCode;

    private LocalDateTime issuedAt;

    private LocalDate dueDate;

    private BigDecimal subtotal;

    private BigDecimal discountTotal;

    private BigDecimal taxTotal;

    private BigDecimal serviceChargeTotal;

    private BigDecimal grandTotal;

    private String status;

    private Long createdById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
