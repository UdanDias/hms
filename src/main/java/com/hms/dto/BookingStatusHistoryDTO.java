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
public class BookingStatusHistoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reservationId;

    private Long reservationRoomId;

    private String oldStatus;

    private String newStatus;

    private String reason;

    private Long changedById;

    private LocalDateTime changedAt;
}
