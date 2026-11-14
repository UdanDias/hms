package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.AccommodationEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for AccommodationEntity; business decisions are made by the service layer. */
@Repository
public class AccommodationDAO extends MutableHibernateDAO<AccommodationEntity> {
    public AccommodationDAO() {
        super(AccommodationEntity.class, "id", AccommodationEntity::getId);
    }

    public List<AccommodationEntity> findByHotelId(Long hotelId) {
        positiveId(hotelId); return select("e.hotel.id = :p0", hotelId);
    }

    public Optional<AccommodationEntity> findByHotelIdAndType(Long hotelId, AccommodationType type) {
        positiveId(hotelId); return selectOne("e.hotel.id = :p0 and e.type = :p1", hotelId, type);
    }

}
