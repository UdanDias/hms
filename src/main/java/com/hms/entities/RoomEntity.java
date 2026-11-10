package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Maps the rooms table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(name = "uk_rooms", columnNames = {"hotel_id", "room_number"}))
public class Room extends AuditableEntity {
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
    @JoinColumn(name = "room_type_id", referencedColumnName = "id", nullable = false)
    private RoomType roomType;

    @NotNull
    @Column(name = "room_number", nullable = false, length = 255)
    private String roomNumber;

    @NotNull
    @Column(name = "floor", nullable = false, length = 255)
    private String floor;

    @NotNull
    @Column(name = "operational_status", nullable = false, length = 255)
    private String operationalStatus;

    @NotNull
    @Column(name = "housekeeping_status", nullable = false, length = 255)
    private String housekeepingStatus;

    @NotNull
    @Column(name = "notes", nullable = false, columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statusId", referencedColumnName = "status_id", nullable = false)
    private RoomStatus roomStatus;

    public Room() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }

    public String getHousekeepingStatus() { return housekeepingStatus; }
    public void setHousekeepingStatus(String housekeepingStatus) { this.housekeepingStatus = housekeepingStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public RoomStatus getRoomStatus() { return roomStatus; }
    public void setRoomStatus(RoomStatus roomStatus) { this.roomStatus = roomStatus; }

}
