package com.hms.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * Maps the reservation_customers table. Relationships are unidirectional; use DTOs at API
 * boundaries.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(
    name = "reservation_customers",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_reservation_customers",
            columnNames = {"reservation_room_id", "customer_id"}))
public class ReservationCustomerEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_room_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ReservationRoomEntity reservationRoom;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private CustomerEntity customer;

  @NotNull
  @Column(name = "is_primary_customer", nullable = false)
  private Boolean isPrimaryCustomer;

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
