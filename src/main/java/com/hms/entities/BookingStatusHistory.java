package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the booking_status_history table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "booking_status_history")
public class BookingStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reservation_room_id", referencedColumnName = "id", nullable = true)
    private ReservationRoom reservationRoom;

    @Column(name = "old_status", nullable = true, length = 255)
    private String oldStatus;

    @NotNull
    @Column(name = "new_status", nullable = false, length = 255)
    private String newStatus;

    @Column(name = "reason", nullable = true, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "changed_by", referencedColumnName = "userId", nullable = true)
    private User changedBy;

    @NotNull
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public BookingStatusHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public ReservationRoom getReservationRoom() { return reservationRoom; }
    public void setReservationRoom(ReservationRoom reservationRoom) { this.reservationRoom = reservationRoom; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

}
