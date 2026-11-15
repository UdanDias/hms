package com.hms.entities;

import com.hms.entities.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Maps the accomodation table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(
    name = "accomodation",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_accomodation",
            columnNames = {"hotel_id", "type"}))
public class AccommodationEntity {
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
  @Column(name = "type", nullable = false)
  private AccommodationType type;

  @NotNull
  @PositiveOrZero
  @Column(name = "price", nullable = false, precision = 12, scale = 2)
  private BigDecimal price;
}
