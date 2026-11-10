package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Maps the accomodation table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "accomodation", uniqueConstraints = @UniqueConstraint(name = "uk_accomodation", columnNames = {"hotel_id", "type"}))
public class AccommodationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
    private HotelEntity hotel;

    @NotNull
    @Column(name = "type", nullable = false)
    private com.hotelmanagement.entity.enums.AccommodationType type;

    @NotNull
    @PositiveOrZero
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public AccommodationEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HotelEntity getHotel() { return hotel; }
    public void setHotel(HotelEntity hotel) { this.hotel = hotel; }

    public com.hotelmanagement.entity.enums.AccommodationType getType() { return type; }
    public void setType(com.hotelmanagement.entity.enums.AccommodationType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

}
