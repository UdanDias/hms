package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Maps the roles table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @NotNull
    @Column(name = "type", nullable = false, unique = true, length = 255)
    private String type;

    public Role() {}

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

}
