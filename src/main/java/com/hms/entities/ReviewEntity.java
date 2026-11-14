package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the reviews table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "reviews")
public class ReviewEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false, unique = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ReservationEntity reservation;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private CustomerEntity customer;

  @NotNull
  @Min(1)
  @Max(5)
  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "title", nullable = true, length = 255)
  private String title;

  @NotNull
  @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
  private String comment;

  @NotNull
  @Column(name = "moderation_status", nullable = false, length = 255)
  private String moderationStatus;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "moderated_by", referencedColumnName = "userId", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity moderatedBy;

  @Column(name = "moderated_at", nullable = true)
  private LocalDateTime moderatedAt;

  @Column(name = "management_reply", nullable = true, columnDefinition = "TEXT")
  private String managementReply;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "replied_by", referencedColumnName = "userId", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity repliedBy;

  @Column(name = "replied_at", nullable = true)
  private LocalDateTime repliedAt;

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
