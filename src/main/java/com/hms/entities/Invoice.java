package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Maps the invoices table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "invoices")
public class Invoice extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
    private Hotel hotel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
    private Reservation reservation;

    @NotNull
    @Column(name = "invoice_number", nullable = false, unique = true, length = 255)
    private String invoiceNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billed_customer_id", referencedColumnName = "id", nullable = false)
    private Customer billedCustomer;

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
    private User createdBy;

    public Invoice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Customer getBilledCustomer() { return billedCustomer; }
    public void setBilledCustomer(Customer billedCustomer) { this.billedCustomer = billedCustomer; }

    public String getBillingNameSnapshot() { return billingNameSnapshot; }
    public void setBillingNameSnapshot(String billingNameSnapshot) { this.billingNameSnapshot = billingNameSnapshot; }

    public String getBillingAddressSnapshot() { return billingAddressSnapshot; }
    public void setBillingAddressSnapshot(String billingAddressSnapshot) { this.billingAddressSnapshot = billingAddressSnapshot; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }

    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }

    public BigDecimal getServiceChargeTotal() { return serviceChargeTotal; }
    public void setServiceChargeTotal(BigDecimal serviceChargeTotal) { this.serviceChargeTotal = serviceChargeTotal; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

}
