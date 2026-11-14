package com.hms.dao;

import com.hms.entity.ReservationCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCustomerDao extends JpaRepository<ReservationCustomerEntity, Long> {
}
