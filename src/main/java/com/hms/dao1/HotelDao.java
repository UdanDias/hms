package com.hms.dao;

import com.hms.entity.HotelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelDao
        extends JpaRepository<HotelEntity, Long> {
}