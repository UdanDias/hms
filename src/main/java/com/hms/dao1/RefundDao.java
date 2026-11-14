package com.hms.dao;

import com.hms.entity.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundDao extends JpaRepository<RefundEntity, Long> {
}
