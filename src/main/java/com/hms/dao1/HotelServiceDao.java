package com.hms.dao;

import com.hms.entity.HotelServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelServiceDao extends JpaRepository<HotelServiceEntity, Long> {
}
