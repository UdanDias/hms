package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.RoomTypeEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Persistence access for RoomTypeEntity; business decisions are made by the service layer. */
@Repository
public class RoomTypeDAO extends MutableHibernateDAO<RoomTypeEntity> {
  public RoomTypeDAO() {
    super(RoomTypeEntity.class, "id", RoomTypeEntity::getId);
  }

  public List<RoomTypeEntity> findByHotelId(Long hotelId) {
    positiveId(hotelId);
    return select("e.hotel.id = :p0", hotelId);
  }

  public List<RoomTypeEntity> findActiveByHotelId(Long hotelId) {
    positiveId(hotelId);
    return select("e.hotel.id = :p0 and e.isActive = true", hotelId);
  }
}
