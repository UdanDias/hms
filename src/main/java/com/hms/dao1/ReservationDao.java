package com.hms.dao;

import com.hms.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDao extends JpaRepository<ReservationEntity, Long> {
}
