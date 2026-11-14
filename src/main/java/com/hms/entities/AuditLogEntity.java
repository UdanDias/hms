package com.hms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Maps the audit_logs table. Relationships are unidirectional; use DTOs at API boundaries. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private HotelEntity hotel;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "actor_user_id", referencedColumnName = "userId", nullable = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private UserEntity actorUser;

  @NotNull
  @Column(name = "action", nullable = false, length = 255)
  private String action;

  @NotNull
  @Column(name = "entity_type", nullable = false, length = 255)
  private String entityType;

  @NotNull
  @Column(name = "entity_id", nullable = false)
  private Long entityId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "old_values", nullable = true, columnDefinition = "JSON")
  private String oldValues;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "new_values", nullable = true, columnDefinition = "JSON")
  private String newValues;

  @Column(name = "request_id", nullable = true, length = 255)
  private String requestId;

  @NotNull
  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;
}
