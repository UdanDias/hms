package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the reservations table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "reservations")
public class ReservationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private HotelEntity hotel;

  @NotNull
  @Column(name = "booking_reference", nullable = false, unique = true, length = 255)
  private String bookingReference;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "booker_customer_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private CustomerEntity bookerCustomer;

  @NotNull
  @Column(name = "status", nullable = false, length = 255)
  private String status;

  @NotNull
  @Column(name = "booking_source", nullable = false, length = 255)
  private String bookingSource;

  @NotNull
  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @NotNull
  @Column(name = "special_requests", nullable = false, columnDefinition = "TEXT")
  private String specialRequests;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "created_by", referencedColumnName = "userId", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity createdBy;

  @Column(name = "confirmed_at", nullable = true)
  private LocalDateTime confirmedAt;

  @Column(name = "cancelled_at", nullable = true)
  private LocalDateTime cancelledAt;

  @Column(name = "cancellation_reason", nullable = true, columnDefinition = "TEXT")
  private String cancellationReason;

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
