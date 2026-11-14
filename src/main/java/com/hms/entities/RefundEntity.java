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

/** Maps the refunds table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "refunds")
public class RefundEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private PaymentEntity payment;

  @NotNull
  @Column(name = "refund_number", nullable = false, unique = true, length = 255)
  private String refundNumber;

  @NotNull
  @PositiveOrZero
  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @NotNull
  @Column(name = "refund_method", nullable = false, length = 255)
  private String refundMethod;

  @Column(name = "reference_number", nullable = true, length = 255)
  private String referenceNumber;

  @NotNull
  @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
  private String reason;

  @NotNull
  @Column(name = "status", nullable = false, length = 255)
  private String status;

  @Column(name = "refunded_at", nullable = true)
  private LocalDateTime refundedAt;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "processed_by", referencedColumnName = "userId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity processedBy;

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
