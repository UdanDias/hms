package com.hms.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReservationCustomerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reservationRoomId;

    private Long customerId;

    private Boolean isPrimaryCustomer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
