package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Maps the room_rates table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(
    name = "room_rates",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_room_rates",
            columnNames = {"room_type_id", "stay_date"}))
public class RoomRateEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_type_id", referencedColumnName = "id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private RoomTypeEntity roomType;

  @NotNull
  @Column(name = "stay_date", nullable = false)
  private LocalDate stayDate;

  @NotNull
  @PositiveOrZero
  @Column(name = "nightly_rate", nullable = false, precision = 12, scale = 2)
  private BigDecimal nightlyRate;

  @NotNull
  @PositiveOrZero
  @Column(name = "extra_adult_rate", nullable = false, precision = 12, scale = 2)
  private BigDecimal extraAdultRate;

  @NotNull
  @PositiveOrZero
  @Column(name = "extra_child_rate", nullable = false, precision = 12, scale = 2)
  private BigDecimal extraChildRate;

  @NotNull
  @Min(1)
  @Column(name = "minimum_nights", nullable = false)
  private Integer minimumNights;

  @NotNull
  @Column(name = "stop_sell", nullable = false)
  private Boolean stopSell;

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
