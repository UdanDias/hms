package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the reservations table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "reservations")
public class Reservation extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
    private Hotel hotel;

    @NotNull
    @Column(name = "booking_reference", nullable = false, unique = true, length = 255)
    private String bookingReference;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booker_customer_id", referencedColumnName = "id", nullable = false)
    private Customer bookerCustomer;

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
    private User createdBy;

    @Column(name = "confirmed_at", nullable = true)
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at", nullable = true)
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", nullable = true, columnDefinition = "TEXT")
    private String cancellationReason;

    public Reservation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public Customer getBookerCustomer() { return bookerCustomer; }
    public void setBookerCustomer(Customer bookerCustomer) { this.bookerCustomer = bookerCustomer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingSource() { return bookingSource; }
    public void setBookingSource(String bookingSource) { this.bookingSource = bookingSource; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

}
