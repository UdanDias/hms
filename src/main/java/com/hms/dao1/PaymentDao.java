package com.hms.dao;

import com.hms.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDao extends JpaRepository<PaymentEntity, Long> {
}
