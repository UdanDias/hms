package com.hms.dao;

import com.hms.entity.RoomTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeDao extends JpaRepository<RoomTypeEntity, Long> {
}
