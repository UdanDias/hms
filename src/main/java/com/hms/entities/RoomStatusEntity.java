package com.hms.entities;

import com.hms.entities.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps the room_status table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "room_status")
public class RoomStatusEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "status_id", nullable = false)
  private Long statusId;

  @NotNull
  @Column(name = "status", nullable = false, unique = true)
  private RoomStatusValue status;
}
