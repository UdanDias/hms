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
public class RoomRateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long roomTypeId;

    private LocalDate stayDate;

    private BigDecimal nightlyRate;

    private BigDecimal extraAdultRate;

    private BigDecimal extraChildRate;

    private Integer minimumNights;

    private Boolean stopSell;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
