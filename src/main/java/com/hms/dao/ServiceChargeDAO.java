package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.ServiceChargeEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for ServiceChargeEntity; business decisions are made by the service layer. */
@Repository
public class ServiceChargeDAO extends MutableHibernateDAO<ServiceChargeEntity> {
    public ServiceChargeDAO() {
        super(ServiceChargeEntity.class, "id", ServiceChargeEntity::getId);
    }

    public List<ServiceChargeEntity> findByReservationId(Long reservationId) {
        positiveId(reservationId); return select("e.reservation.id = :p0", reservationId);
    }

    public List<ServiceChargeEntity> findByReservationRoomId(Long reservationRoomId) {
        positiveId(reservationRoomId); return select("e.reservationRoom.id = :p0", reservationRoomId);
    }

    public List<ServiceChargeEntity> findByServiceId(Long serviceId) {
        positiveId(serviceId); return select("e.service.id = :p0", serviceId);
    }

    public List<ServiceChargeEntity> findByRecordedById(Long recordedById) {
        positiveId(recordedById); return select("e.recordedBy.userId = :p0", recordedById);
    }

    public List<ServiceChargeEntity> findActiveByReservationId(Long reservationId) {
        positiveId(reservationId); return select("e.reservation.id = :p0 and e.status = 'active'", reservationId);
    }

}
