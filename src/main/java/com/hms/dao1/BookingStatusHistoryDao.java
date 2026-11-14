package com.hms.dao;

import com.hms.entity.BookingStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStatusHistoryDao extends JpaRepository<BookingStatusHistoryEntity, Long> {
}
