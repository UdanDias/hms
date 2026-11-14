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
public class RoomDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long hotelId;

    private Long roomTypeId;

    private String roomNumber;

    private String floor;

    private String operationalStatus;

    private String housekeepingStatus;

    private String notes;

    private Long statusId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
