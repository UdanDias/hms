package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the customer table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "customer")
public class CustomerEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = true, unique = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity user;

  @NotNull
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @NotNull
  @Column(name = "phone", nullable = false, length = 255)
  private String phone;

  @Email
  @Column(name = "email", nullable = true, length = 255)
  private String email;

  @Column(name = "nic", nullable = true, length = 255)
  // Encrypt sensitive NIC values in the service layer before storage.
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private String nic;

  @Min(0)
  @Max(150)
  @Column(name = "age", nullable = true)
  // Optional captured age; dateOfBirth is preferred for calculating current age.
  private Integer age;

  @Column(name = "address", nullable = true, columnDefinition = "TEXT")
  private String address;

  @Column(name = "country_code", nullable = true, length = 2)
  private String countryCode;

  @Column(name = "date_of_birth", nullable = true)
  private LocalDate dateOfBirth;

  @Column(name = "identity_document_type", nullable = true, length = 255)
  private String identityDocumentType;

  @Column(name = "identity_document_number_encrypted", nullable = true, length = 1024)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private byte[] identityDocumentNumberEncrypted;

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
