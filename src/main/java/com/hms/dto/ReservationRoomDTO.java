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
public class ReservationRoomDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reservationId;

    private Long roomTypeId;

    private Long roomId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer adults;

    private Integer children;

    private BigDecimal nightlyRate;

    private BigDecimal extraPersonTotal;

    private BigDecimal discountAmount;

    private BigDecimal taxAmount;

    private BigDecimal serviceChargeAmount;

    private BigDecimal totalAmount;

    private String cancellationPolicySnapshot;

    private String status;

    private LocalDateTime actualCheckInAt;

    private LocalDateTime actualCheckOutAt;

    private Long accommodationId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
