package com.hms.entities;

import com.hms.entities.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

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
