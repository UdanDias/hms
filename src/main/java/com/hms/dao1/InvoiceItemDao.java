package com.hms.dao;

import com.hms.entity.InvoiceItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemDao extends JpaRepository<InvoiceItemEntity, Long> {
}
