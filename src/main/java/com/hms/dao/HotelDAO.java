package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.HotelEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for HotelEntity; business decisions are made by the service layer. */
@Repository
public class HotelDAO extends MutableHibernateDAO<HotelEntity> {
    public HotelDAO() {
        super(HotelEntity.class, "id", HotelEntity::getId);
    }

    public List<HotelEntity> findActive() {
        return select("e.isActive = true");
    }

}
