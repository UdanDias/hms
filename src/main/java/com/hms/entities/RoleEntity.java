package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps the roles table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "roles")
public class RoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id", nullable = false)
  private Long roleId;

  @NotNull
  @Column(name = "type", nullable = false, unique = true, length = 255)
  private String type;
}
