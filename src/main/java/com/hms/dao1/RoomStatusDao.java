package com.hms.dao;

import com.hms.entity.RoomStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomStatusDao extends JpaRepository<RoomStatusEntity, Long> {
}
