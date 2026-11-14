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
public class ServiceChargeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reservationId;

    private Long reservationRoomId;

    private Long serviceId;

    private String descriptionSnapshot;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private LocalDateTime providedAt;

    private String status;

    private Long recordedById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
