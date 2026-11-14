package com.hms.dao;

import com.hms.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceDao extends JpaRepository<InvoiceEntity, Long> {
}
