package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.ReviewEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for ReviewEntity; business decisions are made by the service layer. */
@Repository
public class ReviewDAO extends MutableHibernateDAO<ReviewEntity> {
    public ReviewDAO() {
        super(ReviewEntity.class, "id", ReviewEntity::getId);
    }

    public List<ReviewEntity> findByCustomerId(Long customerId) {
        positiveId(customerId); return select("e.customer.id = :p0", customerId);
    }

    public List<ReviewEntity> findByModeratedById(Long moderatedById) {
        positiveId(moderatedById); return select("e.moderatedBy.userId = :p0", moderatedById);
    }

    public List<ReviewEntity> findByRepliedById(Long repliedById) {
        positiveId(repliedById); return select("e.repliedBy.userId = :p0", repliedById);
    }

    public Optional<ReviewEntity> findByReservationId(Long reservationId) {
        positiveId(reservationId); return selectOne("e.reservation.id = :p0", reservationId);
    }

    public List<ReviewEntity> findApprovedByHotelId(Long hotelId) {
        positiveId(hotelId); return selectOrdered("e.reservation.hotel.id = :p0 and e.moderationStatus = 'approved'", "e.createdAt desc, e.id desc", hotelId);
    }

}
