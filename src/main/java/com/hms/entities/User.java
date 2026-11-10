package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Maps the user table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "`user`")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @NotNull
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false, length = 255)
    // Store a password hash, never plaintext. Do not expose in response DTOs.
    private String password;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
    private Role role;

    public User() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

}
