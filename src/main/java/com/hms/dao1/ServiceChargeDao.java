package com.hms.dao;

import com.hms.entity.ServiceChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceChargeDao extends JpaRepository<ServiceChargeEntity, Long> {
}
