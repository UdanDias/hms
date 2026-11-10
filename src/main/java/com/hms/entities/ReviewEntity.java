package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the reviews table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "reviews")
public class Review extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false, unique = true)
    private Reservation reservation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

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
    private User moderatedBy;

    @Column(name = "moderated_at", nullable = true)
    private LocalDateTime moderatedAt;

    @Column(name = "management_reply", nullable = true, columnDefinition = "TEXT")
    private String managementReply;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "replied_by", referencedColumnName = "userId", nullable = true)
    private User repliedBy;

    @Column(name = "replied_at", nullable = true)
    private LocalDateTime repliedAt;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public User getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(User moderatedBy) { this.moderatedBy = moderatedBy; }

    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }

    public String getManagementReply() { return managementReply; }
    public void setManagementReply(String managementReply) { this.managementReply = managementReply; }

    public User getRepliedBy() { return repliedBy; }
    public void setRepliedBy(User repliedBy) { this.repliedBy = repliedBy; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }

}
