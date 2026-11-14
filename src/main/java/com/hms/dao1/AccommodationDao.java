package com.hms.dao;

import com.hms.entity.AccommodationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationDao extends JpaRepository<AccommodationEntity, Long> {
}
