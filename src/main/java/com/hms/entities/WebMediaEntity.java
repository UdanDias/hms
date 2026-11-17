package com.hms.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "web_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebMediaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hotel_id", nullable = false)
  private Long hotelId;

  @Column(name = "owner_type", nullable = false, length = 20)
  private String ownerType;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Column(name = "storage_key", nullable = false, unique = true, length = 80)
  private String storageKey;

  @Column(name = "alt_text", nullable = false, length = 200)
  private String altText;
}
