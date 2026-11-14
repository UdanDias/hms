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
public class AuditLogDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long hotelId;

    private Long actorUserId;

    private String action;

    private String entityType;

    private Long entityId;

    private String oldValues;

    private String newValues;

    private String requestId;

    private LocalDateTime occurredAt;
}
