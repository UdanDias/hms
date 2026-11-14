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

/** Maps the invoices table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "invoices")
public class InvoiceEntity {
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

  @NotNull
  @Column(name = "invoice_number", nullable = false, unique = true, length = 255)
  private String invoiceNumber;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "billed_customer_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private CustomerEntity billedCustomer;

  @NotNull
  @Column(name = "billing_name_snapshot", nullable = false, length = 255)
  private String billingNameSnapshot;

  @NotNull
  @Column(name = "billing_address_snapshot", nullable = false, columnDefinition = "TEXT")
  private String billingAddressSnapshot;

  @NotNull
  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @Column(name = "issued_at", nullable = true)
  private LocalDateTime issuedAt;

  @Column(name = "due_date", nullable = true)
  private LocalDate dueDate;

  @NotNull
  @PositiveOrZero
  @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @NotNull
  @PositiveOrZero
  @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountTotal;

  @NotNull
  @PositiveOrZero
  @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal taxTotal;

  @NotNull
  @PositiveOrZero
  @Column(name = "service_charge_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal serviceChargeTotal;

  @NotNull
  @PositiveOrZero
  @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal grandTotal;

  @NotNull
  @Column(name = "status", nullable = false, length = 255)
  private String status;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "created_by", referencedColumnName = "userId", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity createdBy;

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
