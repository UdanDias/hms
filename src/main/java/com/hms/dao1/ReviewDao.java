package com.hms.dao;

import com.hms.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewDao extends JpaRepository<ReviewEntity, Long> {
}
