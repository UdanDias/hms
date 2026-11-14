package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.time.*;

/**
 * Maps the booking_status_history table. Relationships are unidirectional; use DTOs at API
 * boundaries.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "booking_status_history")
public class BookingStatusHistoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ReservationEntity reservation;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "reservation_room_id", referencedColumnName = "id", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ReservationRoomEntity reservationRoom;

  @Column(name = "old_status", nullable = true, length = 255)
  private String oldStatus;

  @NotNull
  @Column(name = "new_status", nullable = false, length = 255)
  private String newStatus;

  @Column(name = "reason", nullable = true, columnDefinition = "TEXT")
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "changed_by", referencedColumnName = "userId", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity changedBy;

  @NotNull
  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;
}
