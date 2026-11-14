package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.HotelServiceEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for HotelServiceEntity; business decisions are made by the service layer. */
@Repository
public class HotelServiceDAO extends MutableHibernateDAO<HotelServiceEntity> {
    public HotelServiceDAO() {
        super(HotelServiceEntity.class, "id", HotelServiceEntity::getId);
    }

    public List<HotelServiceEntity> findByHotelId(Long hotelId) {
        positiveId(hotelId); return select("e.hotel.id = :p0", hotelId);
    }

    public List<HotelServiceEntity> findActiveByHotelId(Long hotelId) {
        positiveId(hotelId); return select("e.hotel.id = :p0 and e.isActive = true", hotelId);
    }

}
