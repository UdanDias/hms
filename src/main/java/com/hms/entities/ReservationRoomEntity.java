package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/**
 * Maps the reservation_rooms table. Relationships are unidirectional; use DTOs at API boundaries.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "reservation_rooms")
public class ReservationRoomEntity {
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

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_type_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private RoomTypeEntity roomType;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private RoomEntity room;

  @NotNull
  @Column(name = "check_in_date", nullable = false)
  private LocalDate checkInDate;

  @NotNull
  @Column(name = "check_out_date", nullable = false)
  private LocalDate checkOutDate;

  @NotNull
  @Min(0)
  @Column(name = "adults", nullable = false)
  private Integer adults;

  @NotNull
  @Min(0)
  @Column(name = "children", nullable = false)
  private Integer children;

  @NotNull
  @PositiveOrZero
  @Column(name = "nightly_rate", nullable = false, precision = 12, scale = 2)
  private BigDecimal nightlyRate;

  @NotNull
  @PositiveOrZero
  @Column(name = "extra_person_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal extraPersonTotal;

  @NotNull
  @PositiveOrZero
  @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountAmount;

  @NotNull
  @PositiveOrZero
  @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal taxAmount;

  @NotNull
  @PositiveOrZero
  @Column(name = "service_charge_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal serviceChargeAmount;

  @NotNull
  @PositiveOrZero
  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @NotNull
  @Column(name = "cancellation_policy_snapshot", nullable = false, columnDefinition = "TEXT")
  private String cancellationPolicySnapshot;

  @NotNull
  @Column(name = "status", nullable = false, length = 255)
  private String status;

  @Column(name = "actual_check_in_at", nullable = true)
  private LocalDateTime actualCheckInAt;

  @Column(name = "actual_check_out_at", nullable = true)
  private LocalDateTime actualCheckOutAt;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "accomodation_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private AccommodationEntity accommodation;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  private void onCreate() {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now(ZoneOffset.UTC);
  }
}
