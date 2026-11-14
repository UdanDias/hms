package com.hms.dao;

import com.hms.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerDao extends JpaRepository<CustomerEntity, Long> {
}
