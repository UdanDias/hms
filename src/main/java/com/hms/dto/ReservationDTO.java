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
public class ReservationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long hotelId;

    private String bookingReference;

    private Long bookerCustomerId;

    private String status;

    private String bookingSource;

    private String currencyCode;

    private String specialRequests;

    private Long createdById;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
