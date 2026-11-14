package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.RoomRateEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for RoomRateEntity; business decisions are made by the service layer. */
@Repository
public class RoomRateDAO extends MutableHibernateDAO<RoomRateEntity> {
    public RoomRateDAO() {
        super(RoomRateEntity.class, "id", RoomRateEntity::getId);
    }

    public List<RoomRateEntity> findByRoomTypeId(Long roomTypeId) {
        positiveId(roomTypeId); return select("e.roomType.id = :p0", roomTypeId);
    }

    public Optional<RoomRateEntity> findByRoomTypeIdAndStayDate(Long roomTypeId, LocalDate stayDate) {
        positiveId(roomTypeId); return selectOne("e.roomType.id = :p0 and e.stayDate = :p1", roomTypeId, stayDate);
    }

    public List<RoomRateEntity> findByRoomTypeIdAndDateRange(Long roomTypeId, LocalDate start, LocalDate end) {
        positiveId(roomTypeId); dateRange(start, end); return selectOrdered("e.roomType.id = :p0 and e.stayDate >= :p1 and e.stayDate < :p2", "e.stayDate, e.id", roomTypeId, start, end);
    }

}
