package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Maps the reservation_customers table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "reservation_customers", uniqueConstraints = @UniqueConstraint(name = "uk_reservation_customers", columnNames = {"reservation_room_id", "customer_id"}))
public class ReservationCustomer extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_room_id", referencedColumnName = "id", nullable = false)
    private ReservationRoom reservationRoom;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private CustomerEntity customer;

    @NotNull
    @Column(name = "is_primary_customer", nullable = false)
    private Boolean isPrimaryCustomer;

    public ReservationCustomer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReservationRoom getReservationRoom() { return reservationRoom; }
    public void setReservationRoom(ReservationRoom reservationRoom) { this.reservationRoom = reservationRoom; }

    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }

    public Boolean getIsPrimaryCustomer() { return isPrimaryCustomer; }
    public void setIsPrimaryCustomer(Boolean isPrimaryCustomer) { this.isPrimaryCustomer = isPrimaryCustomer; }

}
