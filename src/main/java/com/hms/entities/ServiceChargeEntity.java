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

/** Maps the service_charges table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "service_charges")
public class ServiceChargeEntity {
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

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private HotelServiceEntity service;

  @NotNull
  @Column(name = "description_snapshot", nullable = false, columnDefinition = "TEXT")
  private String descriptionSnapshot;

  @NotNull
  @PositiveOrZero
  @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
  private BigDecimal quantity;

  @NotNull
  @PositiveOrZero
  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

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
  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @NotNull
  @Column(name = "provided_at", nullable = false)
  private LocalDateTime providedAt;

  @NotNull
  @Column(name = "status", nullable = false, length = 255)
  private String status;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recorded_by", referencedColumnName = "userId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity recordedBy;

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
