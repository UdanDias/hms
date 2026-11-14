package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/** Maps the user table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "`user`")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "userId", nullable = false)
  private Long userId;

  @NotNull
  @Column(name = "username", nullable = false, unique = true, length = 255)
  private String username;

  @NotNull
  @Email
  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @NotNull
  @Column(name = "password", nullable = false, length = 255)
  // Store a password hash, never plaintext. Do not expose in response DTOs.
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private String password;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private RoleEntity role;
}
