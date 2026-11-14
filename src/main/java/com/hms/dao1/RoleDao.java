package com.hms.dao;

import com.hms.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<RoleEntity, Long> {
}
