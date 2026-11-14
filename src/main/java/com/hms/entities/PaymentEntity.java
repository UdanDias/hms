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

/** Maps the payments table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "payments")
public class PaymentEntity {
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
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ReservationEntity reservation;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "invoice_id", referencedColumnName = "id", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private InvoiceEntity invoice;

  @NotNull
  @Column(name = "receipt_number", nullable = false, unique = true, length = 255)
  private String receiptNumber;

  @NotNull
  @PositiveOrZero
  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @NotNull
  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @NotNull
  @Column(name = "payment_method", nullable = false, length = 255)
  private String paymentMethod;

  @Column(name = "reference_number", nullable = true, length = 255)
  private String referenceNumber;

  @NotNull
  @Column(name = "paid_at", nullable = false)
  private LocalDateTime paidAt;

  @NotNull
  @Column(name = "status", nullable = false, length = 255)
  private String status;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "received_by", referencedColumnName = "userId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity receivedBy;

  @Column(name = "notes", nullable = true, columnDefinition = "TEXT")
  private String notes;

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
