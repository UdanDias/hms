package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Maps the reservation_rooms table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "reservation_rooms")
public class ReservationRoom extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
    private Reservation reservation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", referencedColumnName = "id", nullable = false)
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = true)
    private Room room;

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
    private AccommodationEntity accommodation;

    public ReservationRoom() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }

    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }

    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }

    public BigDecimal getExtraPersonTotal() { return extraPersonTotal; }
    public void setExtraPersonTotal(BigDecimal extraPersonTotal) { this.extraPersonTotal = extraPersonTotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getServiceChargeAmount() { return serviceChargeAmount; }
    public void setServiceChargeAmount(BigDecimal serviceChargeAmount) { this.serviceChargeAmount = serviceChargeAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCancellationPolicySnapshot() { return cancellationPolicySnapshot; }
    public void setCancellationPolicySnapshot(String cancellationPolicySnapshot) { this.cancellationPolicySnapshot = cancellationPolicySnapshot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getActualCheckInAt() { return actualCheckInAt; }
    public void setActualCheckInAt(LocalDateTime actualCheckInAt) { this.actualCheckInAt = actualCheckInAt; }

    public LocalDateTime getActualCheckOutAt() { return actualCheckOutAt; }
    public void setActualCheckOutAt(LocalDateTime actualCheckOutAt) { this.actualCheckOutAt = actualCheckOutAt; }

    public AccommodationEntity getAccommodation() { return accommodation; }
    public void setAccommodation(AccommodationEntity accommodation) { this.accommodation = accommodation; }

}
