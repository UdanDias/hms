package com.hms.entities;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "web_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebBookingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id", unique = true, nullable = false)
  private ReservationEntity reservation;

  @Column(name = "request_key", unique = true, nullable = false, length = 80)
  private String requestKey;

  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  @Column(name = "access_hash", nullable = false, length = 64)
  private String accessHash;

  @Column(name = "access_expires", nullable = false)
  private Instant accessExpires;

  @Lob
  @Column(name = "snapshot_json", nullable = false, columnDefinition = "LONGTEXT")
  private String snapshotJson;
}
