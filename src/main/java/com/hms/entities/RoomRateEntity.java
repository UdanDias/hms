package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Maps the room_rates table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "room_rates", uniqueConstraints = @UniqueConstraint(name = "uk_room_rates", columnNames = {"room_type_id", "stay_date"}))
public class RoomRate extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", referencedColumnName = "id", nullable = false)
    private RoomType roomType;

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

    public RoomRate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public LocalDate getStayDate() { return stayDate; }
    public void setStayDate(LocalDate stayDate) { this.stayDate = stayDate; }

    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }

    public BigDecimal getExtraAdultRate() { return extraAdultRate; }
    public void setExtraAdultRate(BigDecimal extraAdultRate) { this.extraAdultRate = extraAdultRate; }

    public BigDecimal getExtraChildRate() { return extraChildRate; }
    public void setExtraChildRate(BigDecimal extraChildRate) { this.extraChildRate = extraChildRate; }

    public Integer getMinimumNights() { return minimumNights; }
    public void setMinimumNights(Integer minimumNights) { this.minimumNights = minimumNights; }

    public Boolean getStopSell() { return stopSell; }
    public void setStopSell(Boolean stopSell) { this.stopSell = stopSell; }

}
