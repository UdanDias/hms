package com.hms.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/** Maps the rooms table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(
    name = "rooms",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_rooms",
            columnNames = {"hotel_id", "room_number"}))
public class RoomEntity {
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
  @JoinColumn(name = "room_type_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private RoomTypeEntity roomType;

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
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private RoomStatusEntity roomStatus;

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
