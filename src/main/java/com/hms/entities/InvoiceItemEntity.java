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
import java.math.BigDecimal;

/** Maps the invoice_items table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "invoice_items")
public class InvoiceItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "invoice_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private InvoiceEntity invoice;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "reservation_room_id", referencedColumnName = "id", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ReservationRoomEntity reservationRoom;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "service_charge_id", referencedColumnName = "id", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ServiceChargeEntity serviceCharge;

  @NotNull
  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

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
  @Column(name = "tax_rate", nullable = false, precision = 7, scale = 4)
  private BigDecimal taxRate;

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
  @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal lineTotal;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "accomodation_id", referencedColumnName = "id", nullable = true)
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
