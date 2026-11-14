package com.hms.dao;

import com.hms.entity.RoomRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRateDao extends JpaRepository<RoomRateEntity, Long> {
}
