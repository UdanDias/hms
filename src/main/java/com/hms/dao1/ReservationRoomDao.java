package com.hms.dao;

import com.hms.entity.ReservationRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRoomDao extends JpaRepository<ReservationRoomEntity, Long> {
}
