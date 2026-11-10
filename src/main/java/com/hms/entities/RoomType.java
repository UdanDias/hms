package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Maps the room_types table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "room_types")
public class RoomType extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
    private HotelEntity hotel;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Min(0)
    @Column(name = "max_adults", nullable = false)
    private Integer maxAdults;

    @NotNull
    @Min(0)
    @Column(name = "max_children", nullable = false)
    private Integer maxChildren;

    @NotNull
    @Min(0)
    @Column(name = "max_occupancy", nullable = false)
    private Integer maxOccupancy;

    @NotNull
    @Column(name = "bed_description", nullable = false, length = 255)
    private String bedDescription;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public RoomType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HotelEntity getHotel() { return hotel; }
    public void setHotel(HotelEntity hotel) { this.hotel = hotel; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }

    public Integer getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(Integer maxOccupancy) { this.maxOccupancy = maxOccupancy; }

    public String getBedDescription() { return bedDescription; }
    public void setBedDescription(String bedDescription) { this.bedDescription = bedDescription; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

}
