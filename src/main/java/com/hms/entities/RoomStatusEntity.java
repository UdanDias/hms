package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.hms.entities.enums.*;

/** Maps the room_status table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "room_status")
public class RoomStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @NotNull
    @Column(name = "status", nullable = false, unique = true)
    private RoomStatusValue status;

    public RoomStatus() {}

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public RoomStatusValue getStatus() { return status; }
    public void setStatus(RoomStatusValue status) { this.status = status; }

}
