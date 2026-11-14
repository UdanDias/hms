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
public class ReviewDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reservationId;

    private Long customerId;

    private Integer rating;

    private String title;

    private String comment;

    private String moderationStatus;

    private Long moderatedById;

    private LocalDateTime moderatedAt;

    private String managementReply;

    private Long repliedById;

    private LocalDateTime repliedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
