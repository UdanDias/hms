package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the hotels table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "hotels")
public class HotelEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @NotNull
  @Column(name = "address", nullable = false, columnDefinition = "TEXT")
  private String address;

  @NotNull
  @Column(name = "city", nullable = false, length = 255)
  private String city;

  @NotNull
  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @NotNull
  @Column(name = "phone", nullable = false, length = 255)
  private String phone;

  @NotNull
  @Email
  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @NotNull
  @Column(name = "timezone", nullable = false, length = 255)
  private String timezone;

  @NotNull
  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @NotNull
  @Column(name = "check_in_time", nullable = false)
  private LocalTime checkInTime;

  @NotNull
  @Column(name = "check_out_time", nullable = false)
  private LocalTime checkOutTime;

  @NotNull
  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

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
