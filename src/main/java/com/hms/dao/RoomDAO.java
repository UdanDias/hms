package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.RoomEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for RoomEntity; business decisions are made by the service layer. */
@Repository
public class RoomDAO extends MutableHibernateDAO<RoomEntity> {
    public RoomDAO() {
        super(RoomEntity.class, "id", RoomEntity::getId);
    }

    public List<RoomEntity> findByHotelId(Long hotelId) {
        positiveId(hotelId); return select("e.hotel.id = :p0", hotelId);
    }

    public List<RoomEntity> findByRoomTypeId(Long roomTypeId) {
        positiveId(roomTypeId); return select("e.roomType.id = :p0", roomTypeId);
    }

    public List<RoomEntity> findByRoomStatusId(Long roomStatusId) {
        positiveId(roomStatusId); return select("e.roomStatus.statusId = :p0", roomStatusId);
    }

    public Optional<RoomEntity> findByHotelIdAndRoomNumber(Long hotelId, String roomNumber) {
        positiveId(hotelId); return selectOne("e.hotel.id = :p0 and e.roomNumber = :p1", hotelId, roomNumber);
    }

    public List<RoomEntity> findSellableByRoomTypeId(Long roomTypeId) {
        positiveId(roomTypeId); return select("e.roomType.id = :p0 and e.operationalStatus = :p1 and e.roomStatus.status = :p2", roomTypeId, "active", RoomStatusValue.AVAILABLE);
    }

}
