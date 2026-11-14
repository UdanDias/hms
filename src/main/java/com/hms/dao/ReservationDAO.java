package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.ReservationEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for ReservationEntity; business decisions are made by the service layer. */
@Repository
public class ReservationDAO extends MutableHibernateDAO<ReservationEntity> {
    public ReservationDAO() {
        super(ReservationEntity.class, "id", ReservationEntity::getId);
    }

    public List<ReservationEntity> findByHotelId(Long hotelId) {
        positiveId(hotelId); return select("e.hotel.id = :p0", hotelId);
    }

    public List<ReservationEntity> findByBookerCustomerId(Long bookerCustomerId) {
        positiveId(bookerCustomerId); return select("e.bookerCustomer.id = :p0", bookerCustomerId);
    }

    public List<ReservationEntity> findByCreatedById(Long createdById) {
        positiveId(createdById); return select("e.createdBy.userId = :p0", createdById);
    }

    public Optional<ReservationEntity> findByBookingReference(String bookingReference) {
        return selectOne("e.bookingReference = :p0", bookingReference);
    }

    public List<ReservationEntity> findByHotelIdAndStatus(Long hotelId, String status) {
        positiveId(hotelId); return selectOrdered("e.hotel.id = :p0 and e.status = :p1", "e.createdAt desc, e.id desc", hotelId, status);
    }

}
